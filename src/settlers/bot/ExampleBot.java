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
            for (Xing x : Board.adjacentXings(c))
                if (api.townAt(x) != null && api.townAt(x).player() == api.me())
                    continue c;
            if (robbable.isEmpty())
                return Pair.make(c, null);
            return Pair.make(c, robbable.get(0));
        }
        return null;
    }

    public void makeTurn() {
        Player me = api.me();
        ResourceStack cards = api.cards();
        if (api.rollDice() == 7) {
            Pair<Hex, Player> rob = rob();
            api.moveRobber(rob.first(), rob.second());
        }
        if (api.developments().knight() > 0) {
            boolean bad = api.largestArmy() != me || api.armyStrength(me) < 3;
            for (Player p : api.robbable(api.robber()))
                bad |= p == me;
            if (bad) {
                Pair<Hex, Player> rob = rob();
                api.knight(rob.first(), rob.second());
            }
        }
        if (api.developments().monopoly() > 0) {
            api.monopoly(cards.ore() >= 3 && cards.grain() < 4 ? Resource.GRAIN : Resource.ORE);
        }
        while (api.citiesLeft() > 0 && api.getIfPossible("OOOGG")) {
            boolean can = false;
            for (Xing i : Board.allXings()) {
                Town t = api.townAt(i);
                if (t == null || t.player() != me || t.isCity())
                    continue;
                api.buildCity(i);
                can = true;
                break;
            }
            if (!can) break;
        }
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
        if (api.developments().roadBuilding() > 0 && api.roadsLeft() > 0) {
            // TODO: the right behaviour
            Path[] roads = new Path[2];
            int inp = 0;
            for (Path p : Board.allPaths()) {
                if (api.canBuildRoadAt(p)) {
                    roads[inp++] = p;
                    if (inp == 2)
                        break;
                }
            }
            if ((inp == 2 && api.roadsLeft() >= 2) || (inp == 1 && api.roadsLeft() == 1))
                api.roadBuilding(roads[0], roads[1]);
        }
        while (api.settlementsLeft() > 0 && api.getIfPossible("BWGL")) {
            boolean can = false;
            for (Xing i : Board.allXings()) {
                if (api.canBuildTownAt(i, true)) {
                    api.buildSettlement(i);
                    can = true;
                    break;
                }
            }
            if (!can) break;
        }
        while (api.developmentsLeft() > 0 && api.getIfPossible("WOG")) {
            api.drawDevelopment();
        }
        
        wt: while (true) {
            for (Xing x : Board.allXings())
                if (api.canBuildTownAt(x, true))
                    break wt;

            if (api.roadsLeft() == 0 || !api.getIfPossible("BL"))
                break wt;

            List<Path> possible = new ArrayList<Path>();
            for (Path p : Board.allPaths())
                if (api.canBuildRoadAt(p))
                    possible.add(p);
            if (possible.isEmpty())
                break wt;
            Collections.sort(possible, new Comparator<Path>() {
                int value(Path p) {
                    int value = 0;
                    Xing[] x = Board.endpoints(p);
                    if (api.canBuildTownAt(x[0], false))
                        value += 100;
                    if (api.canBuildTownAt(x[1], false))
                        value += 100;
                    for (Xing z : x) {
                        boolean me = false, enemy = false;
                        for (Path q : Board.adjacentPaths(z)) {
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
                public int compare(Path p1, Path p2) {
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
        return api.declineOffer(offer);
    }

    public List<Resource> discardHalfOfTheCards() {
        List<Resource> cards = Util.shuffle(api.cards().list(), rnd);
        List<Resource> ans = new ArrayList<Resource>();
        for (int i = 0; i < cards.size() / 2; i++)
            ans.add(cards.get(i));
        return ans;
    }

    public Pair<Xing, Path> placeInitialSettlements(boolean first) {
        List<Xing> l = new ArrayList<Xing>();
        for (Xing i : Board.allXings())
            if (api.canBuildTownAt(i, false))
                l.add(i);

        Collections.sort(l, new Comparator<Xing>() {
            private int sum(Xing a) {
                int ans = 0;
                for (Hex c : Board.adjacentHexes(a)) {
                    int x = api.board().numberAt(c);
                    ans += Math.max(6 - Math.abs(x - 7), 0);
                }
                return ans;
            }
            public int compare(Xing a, Xing b) {
                int sa = sum(a), sb = sum(b);
                if (sa < sb) return 1;
                if (sa > sb) return -1;
                boolean pa = api.board().portAt(a).first();
                boolean pb = api.board().portAt(b).first();
                if (!pa && pb) return 1;
                if (pa && !pb) return -1;
                return 0;
            }
        });

        Xing i = l.get(0);
        List<Path> paths = Board.adjacentPaths(i);
        int pathno = rnd.nextInt(paths.size());
        return Pair.make(i, paths.get(pathno));
    }

}

