package settlers.bot;

import java.util.*;
import settlers.*;
import settlers.util.*;

public class ExampleBot extends Bot {

    private final Random rnd;

    public ExampleBot(Game.API api) {
        super(api);
        rnd = api.rnd();
    }

    
    Pair<Hex, Player> rob() {
        c: for (Hex c : Util.shuffle(Board.allHexes(), rnd)) {
            if (api.robber() == c)
                continue;
            List<Player> robbable = api.robbable(c);
            for (Node x : Board.adjacentNodes(c))
                if (api.townAt(x) != null && api.townAt(x).player() == api.me())
                    continue c;
            if (robbable.isEmpty())
                return Pair.make(c, null);
            return Pair.make(c, robbable.get(0));
        }
        return null;
    }

    Map<Resource, Integer> income() {
        Map<Resource, Integer> ans = new EnumMap<Resource, Integer>(Resource.class);
        for (Resource r : Resource.values())
            ans.put(r, 0);
        for (Node n : Board.allNodes()) {
            Town t = api.townAt(n);
            if (t == null || t.player() != api.me())
                continue;
            int coeff = t.isCity() ? 2 : 1;
            for (Hex h : Board.adjacentHexes(n)) {
                Resource r = api.board().resourceAt(h);
                if (r != null) {
                    int number = api.board().numberAt(h);
                    int old = ans.get(r);
                    ans.put(r, old + 6 - Math.abs(7 - number) * coeff);
                }
            }
        }
        return ans;
    }

    public void makeTurn() {
        Player me = api.me();
        ResourceDeck cards = api.cards();
        Board board = api.board();

        // roll the dice and, if 7, move the robber
        if (api.rollDice() == 7) {
            Pair<Hex, Player> rob = rob();
            api.moveRobber(rob.first(), rob.second());
        }

        // if we have a knight, play it
        if (api.developments().knight() > 0) {
            boolean bad = api.largestArmy() != me || api.armyStrength(me) < 3;
            for (Player p : api.robbable(api.robber()))
                bad |= p == me;
            if (bad) {
                Pair<Hex, Player> rob = rob();
                api.knight(rob.first(), rob.second());
            }
        }

        // if we have a monopoly, play it
        if (api.developments().monopoly() > 0) {
            Resource take = null;
            int min = Integer.MAX_VALUE;
            for (Resource r : Resource.values()) {
                int x = api.bank().howMany(r) + cards.howMany(r);
                if (x < min) {
                    min = x;
                    take = r;
                }
            }
            api.monopoly(take);
        }

        // if we can obtain the resources, build a city
        while (api.citiesLeft() > 0 && api.getIfPossible("OOOGG")) {
            boolean can = false;
            for (Node i : Board.allNodes()) {
                Town t = api.townAt(i);
                if (t == null || t.player() != me || t.isCity())
                    continue;
                api.buildCity(i);
                can = true;
                break;
            }
            if (!can) break;
        }

        // if we have an invention, play it on resources we need most
        if (api.developments().invention() > 0) {
            Resource[] invent = new Resource[5];
            int inp = 0;
            Resource[] priority = {
                Resource.BRICK,
                Resource.LUMBER,
                Resource.GRAIN,
                Resource.WOOL,
                Resource.ORE
            };
            for (Resource r : priority)
                if (cards.howMany(r) == 0)
                    invent[inp++] = r;
            if (inp < 2) {
                invent[inp++] = Resource.GRAIN;
                invent[inp] = Resource.ORE;
            }
            api.invention(invent[0], invent[1]);
        }

        // if we can obtain the resources, build roads
        if (api.developments().roadBuilding() > 0 && api.roadsLeft() > 0) {
            // TODO: calculate roads' priority
            Edge[] roads = new Edge[2];
            int inp = 0;
            for (Edge p : Board.allEdges()) {
                if (api.canBuildRoadAt(p)) {
                    roads[inp++] = p;
                    if (inp == 2)
                        break;
                }
            }
            if ((inp == 2 && api.roadsLeft() >= 2) || (inp == 1 && api.roadsLeft() == 1))
                api.roadBuilding(roads[0], roads[1]);
        }

        // if we can obtain the resources, build a settlement
        while (api.settlementsLeft() > 0 && api.getIfPossible("BWGL")) {
            boolean can = false;
            for (Node i : Board.allNodes()) {
                if (api.canBuildSettlementAt(i, true)) {
                    api.buildSettlement(i);
                    can = true;
                    break;
                }
            }
            if (!can) break;
        }

        // trade the least valuable resource to the most valuable one, if we have any
        Map<Resource, Integer> income = income();
        int maxIncome = 0;
        int minIncome = Integer.MAX_VALUE;
        for (Resource r : income.keySet()) {
            int n = income.get(r);
            maxIncome = Math.max(maxIncome, n);
            minIncome = Math.min(minIncome, n);
        }
        trade: for (Resource sell : Resource.values()) {
            if (income.get(sell) != maxIncome || cards.howMany(sell) == 0)
                continue;
            for (Resource buy : Resource.values()) {
                if (income.get(buy) != minIncome)
                    continue;
                List<TradeResult> results = Util.shuffle(api.trade(sell.chr() + "", buy.chr() + ""), api.rnd());
                for (TradeResult result : results) {
                    if (result.isAccepted()) {
                        result.complete();
                        continue trade;
                    }
                }
            }
        }

        // if we can obtain the resources, draw a development card
        while (api.developmentsLeft() > 0 && api.getIfPossible("WOG")) {
            api.drawDevelopment();
        }
        
        // build roads while there're no places to build a settlement
        wt: while (true) {
            for (Node x : Board.allNodes())
                if (api.canBuildSettlementAt(x, true))
                    break wt;

            if (api.roadsLeft() == 0 || !api.getIfPossible("BL"))
                break wt;

            List<Edge> possible = new ArrayList<Edge>();
            for (Edge p : Board.allEdges())
                if (api.canBuildRoadAt(p))
                    possible.add(p);
            if (possible.isEmpty())
                break wt;
            Collections.sort(possible, new Comparator<Edge>() {
                int value(Edge p) {
                    int value = 0;
                    Node[] x = Board.endpoints(p);
                    if (api.canBuildSettlementAt(x[0], false))
                        value += 100;
                    if (api.canBuildSettlementAt(x[1], false))
                        value += 100;
                    for (Node z : x) {
                        boolean me = false, enemy = false;
                        for (Edge q : Board.adjacentEdges(z)) {
                            Player pl = api.roadAt(q);
                            if (pl == api.me())
                                me = true;
                            else if (pl != null)
                                enemy = true;
                        }
                        if (!me && enemy)
                            value -= 50;
                    }
                    return value + api.roadLengthWith(api.me(), p);
                }
                public int compare(Edge p1, Edge p2) {
                    return value(p2) - value(p1);
                }
            });
            api.buildRoad(possible.get(0));
        }
    }

    public String toString() {
        return "Example Bot";
    }

    public TradeResult trade(TradeOffer offer) {
        // accept if proposed resources are far more valuable than requested
        Map<Resource, Integer> income = income();
        int sells = 0;
        for (char c : offer.sell().toCharArray())
            sells += income.get(Resource.fromChar(c));
        int buys = 0;
        for (char c : offer.buy().toCharArray())
            buys += income.get(Resource.fromChar(c));
        return sells < buys / 2 ? api.acceptOffer(offer) : api.declineOffer(offer);
    }

    public List<Resource> discardHalfOfTheCards() {
        List<Resource> cards = Util.shuffle(api.cards().list(), rnd);
        List<Resource> ans = new ArrayList<Resource>();
        for (int i = 0; i < cards.size() / 2; i++)
            ans.add(cards.get(i));
        return ans;
    }

    public Pair<Node, Edge> placeInitialSettlements(boolean first) {
        List<Node> l = new ArrayList<Node>();
        for (Node i : Board.allNodes())
            if (api.canBuildSettlementAt(i, false))
                l.add(i);

        Collections.sort(l, new Comparator<Node>() {
            private int sum(Node a) {
                int ans = 0;
                for (Hex c : Board.adjacentHexes(a)) {
                    int x = api.board().numberAt(c);
                    ans += Math.max(6 - Math.abs(x - 7), 0);
                }
                return ans;
            }
            public int compare(Node a, Node b) {
                int sa = sum(a), sb = sum(b);
                if (sa < sb) return 1;
                if (sa > sb) return -1;
                boolean pa = api.board().harborAt(a) != null;
                boolean pb = api.board().harborAt(b) != null;
                if (!pa && pb) return 1;
                if (pa && !pb) return -1;
                return 0;
            }
        });

        Node i = l.get(0);
        List<Edge> edges = Board.adjacentEdges(i);
        int edgeno = rnd.nextInt(edges.size());
        return Pair.make(i, edges.get(edgeno));
    }

}

