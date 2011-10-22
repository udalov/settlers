package settlers.bot;

import java.util.*;
import settlers.*;
import settlers.util.*;

public class ExampleBot extends Bot {

    private Random rnd = api.rnd();
    private final Board board;
    
    public ExampleBot(Game.API api) {
        super(api);
        board = api.board();
    }

    
    Pair<Hex, Player> rob() {
        List<Hex> hexes = new ArrayList<Hex>(Board.allHexes());
        Collections.shuffle(hexes, rnd);
        c: for (Hex c : hexes) {
            if (board.robber() == c)
                continue;
            List<Town> ts = board.adjacentTowns(c); 
            if (ts.isEmpty())
                continue;
            for (Town t : ts)
                if (t.player() == api.me())
                    continue c;
            Player rob = null;
            for (Town t : ts)
                if (t.player().cardsNumber() > 0)
                    rob = t.player();
            return Pair.make(c, rob);
        }
        return null;
    }

    public void makeTurn() {
        Player me = api.me();
        CardStack cards = api.cards();
        if (api.rollDice() == 7) {
            Pair<Hex, Player> rob = rob();
            api.moveRobber(rob.first(), rob.second());
        }
        if (api.developments().knight() > 0) {
            Hex robber = board.robber();
            boolean bad = false;
            for (Town ts : board.adjacentTowns(robber))
                bad |= ts.player() == me;
            if (bad) {
                Pair<Hex, Player> rob = rob();
                api.knight(rob.first(), rob.second());
            }
        }
        if (api.developments().monopoly() > 0) {
            api.monopoly(cards.ore() >= 3 && cards.grain() < 4 ? Resource.GRAIN : Resource.ORE);
        }
        while (me.citiesLeft() > 0 && api.getIfPossible("OOOGG")) {
            boolean can = false;
            for (Xing i : Board.allXings()) {
                Town t = board.townAt(i);
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
                invent[inp++] = Resource.ORE;
            }
            api.invention(invent[0], invent[1]);
        }
        if (api.developments().roadBuilding() > 0) {
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
            if ((inp == 2 && me.roadsLeft() >= 2) || (inp == 1 && me.roadsLeft() == 1))
                api.roadBuilding(roads[0], roads[1]);
        }
        while (me.settlementsLeft() > 0 && api.getIfPossible("BWGL")) {
            boolean can = false;
            for (Xing i : Board.allXings()) {
                if (api.canBuildTownAt(i)) {
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
        while (me.roadsLeft() > 0 && api.getIfPossible("BL")) {
            boolean can = false;
            for (Path p : Board.allPaths()) {
                if (api.canBuildRoadAt(p)) {
                    api.buildRoad(p);
                    can = true;
                    break;
                }
            }
            if (!can) break;
        }
    }

    public String toString() {
        return "Example Bot";
    }

    public TradeResult trade(TradeOffer offer) {
        return offer.decline();
    }

    public List<Resource> discardHalfOfTheCards() {
        List<Resource> cards = api.cards().list();
        Collections.shuffle(cards, rnd);
        List<Resource> ans = new ArrayList<Resource>();
        for (int i = 0; i < cards.size() / 2; i++)
            ans.add(cards.get(i));
        return ans;
    }

    public void tradeWithOtherPlayerOnHisTurn() {
    }

    public Pair<Xing, Path> placeFirstSettlements(boolean first) {
        List<Xing> l = new ArrayList<Xing>();
        for (Xing i : Board.allXings())
            if (api.canBuildFirstTownsAt(i))
                l.add(i);

        Collections.sort(l, new Comparator<Xing>() {
            private int sum(Xing a) {
                int ans = 0;
                for (Hex c : Board.adjacentHexes(a)) {
                    int x = api.board().numberAt(c);
                    if (x != 0)
                        ans += 6 - Math.abs(x - 7);
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

