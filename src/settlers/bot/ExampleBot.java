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

    public void makeTurn() {
        Player me = api.me();
        CardStack cards = api.cards();
        if (api.rollDice() == 7) {
            c: for (Board.Cell c : Board.allCells()) {
                if (board.robber() == c)
                    continue;
                List<Town> ts = board.adjacentTowns(c); 
                if (ts.isEmpty())
                    continue;
                for (Town t : ts)
                    if (t.player() == me)
                        continue c;
                Player rob = null;
                for (Town t : ts)
                    if (t.player().cardsNumber() > 0)
                        rob = t.player();
                api.moveRobber(c, rob);
                break;
            }
        }
        if (me.citiesLeft() > 0 && cards.areThere("OOOGG")) {
            for (Board.Intersection i : Board.allIntersections()) {
                Town t = board.townAt(i);
                if (t == null || t.player() != me || t.isCity())
                    continue;
                api.buildCity(i);
                break;
            }
        }
        if (me.settlementsLeft() > 0 && cards.areThere("BWGL")) {
            for (Board.Intersection i : Board.allIntersections()) {
                if (api.canBuildTownAt(i)) {
                    api.buildSettlement(i);
                    break;
                }
            }
        }
        if (me.roadsLeft() > 0 && cards.areThere("BL")) {
            for (Board.Path p : Board.allPaths()) {
                if (api.canBuildRoadAt(p)) {
                    api.buildRoad(p);
                    if (!cards.areThere("BL") || me.roadsLeft() == 0)
                        break;
                }
            }
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

    public Pair<Board.Intersection, Board.Path> placeFirstSettlements(boolean first) {
        List<Board.Intersection> l = new ArrayList<Board.Intersection>();
        for (Board.Intersection i : Board.allIntersections())
            if (api.canBuildFirstTownsAt(i))
                l.add(i);

        Collections.sort(l, new Comparator<Board.Intersection>() {
            private int sum(Board.Intersection a) {
                int ans = 0;
                for (Board.Cell c : Board.adjacentCells(a)) {
                    int x = api.board().numberAt(c);
                    if (x != 0)
                        ans += 6 - Math.abs(x - 7);
                }
                return ans;
            }
            public int compare(Board.Intersection a, Board.Intersection b) {
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

        Board.Intersection i = l.get(0);
        List<Board.Path> paths = Board.adjacentPaths(i);
        int pathno = rnd.nextInt(paths.size());
        return Pair.make(i, paths.get(pathno));
    }

}

