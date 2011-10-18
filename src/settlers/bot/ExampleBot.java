package settlers.bot;

import java.util.*;
import settlers.*;
import settlers.util.*;

public class ExampleBot extends Bot {

    private Random rnd = new Random();
    
    public ExampleBot(Game game) {
        super(game);
    }

    public String getName() {
        return "Example Bot";
    }

    public void makeTurn() {
    }

    public TradeResult trade(TradeOffer offer) {
        return offer.decline();
    }

    public void discardHalfOfTheCards() {
    }

    public void tradeWithOtherPlayerOnHisTurn() {
    }

    public Pair<Board.Intersection, Board.Path> placeFirstSettlements(boolean first) {
        /*
        while (true) {
            int cellno = rnd.nextInt(19);
            Board.Cell cell = Board.allCells().get(cellno);
            int dir = rnd.nextInt(6);
            Board.Intersection ints = Board.ints(cell, dir);
            if (!game.board().canBuildTownAt(ints))
                continue;
            for (Board.Path p : Board.allPaths())
                if (Board.areAdjacent(ints, p))
                    return Pair.make(ints, p);
            return null;
        }
        */
        
        List<Board.Intersection> l = new ArrayList<Board.Intersection>(Board.allIntersections());
        Collections.sort(l, new Comparator<Board.Intersection>() {
            private int sum(Board.Intersection a) {
                int ans = 0;
                for (Board.Cell c : Board.adjacentCells(a)) {
                    Integer x = game.board().numberAt(c);
                    if (x != null)
                        ans += 6 - Math.abs(x - 7);
                }
                return ans;
            }
            public int compare(Board.Intersection a, Board.Intersection b) {
                int sa = sum(a), sb = sum(b);
                if (sa < sb) return 1;
                if (sa > sb) return -1;
                boolean pa = game.board().portAt(a).first();
                boolean pb = game.board().portAt(b).first();
                if (!pa && pb) return 1;
                if (pa && !pb) return -1;
                return 0;
            }
        });

        for (Board.Intersection i : l) {
            if (game.board().canBuildTownAt(i)) {
                List<Board.Path> paths = Board.adjacentPaths(i);
                int pathno = rnd.nextInt(paths.size());
                return Pair.make(i, paths.get(pathno));
            }
        }

        return null;
    }

}

