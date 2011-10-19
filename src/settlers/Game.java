package settlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import settlers.bot.Bot;
import settlers.util.Pair;

public class Game {
    
    public class API {
        private Bot bot;

        API() { }

        public Game game() { return Game.this; }

        void setBot(Bot bot) { this.bot = bot; }
    }
    
    private final List<Player> players = new ArrayList<Player>();
    private final Board board;

    private int turnNumber;
    private int whichPlayerTurn;
    private CardStack[] cards;

    Game() {
        board = Board.create();
    }

    public List<Player> players() { return Collections.unmodifiableList(players); }
    public Board board() { return board; }
    
    public int turnNumber() { return turnNumber; }
    public int whichPlayerTurn() { return whichPlayerTurn; }


    public TradeOffer createTradeOffer(
        Bot bot,
        Map<Resource, Integer> iWant,
        Map<Resource, Integer> iGive
    ) {
        return new TradeOffer(player(bot), iWant, iGive);
    }


    void addPlayer(Player player) {
        players.add(player);
    }

    void play() {
        int n = players.size();
        Collections.shuffle(players);
        cards = new CardStack[n];
        for (int i = 0; i < n; i++) {
            cards[i] = new CardStack();
        }

        for (int it = 0; it < 2; it++) {
            for (int i = it * (n - 1); 0 <= i && i < n; i += 1 - 2*it) {
                Player player = players.get(i);
                Pair<Board.Intersection, Board.Path> p = player.bot().placeFirstSettlements(it == 0);
                if (!board.areAdjacent(p.first(), p.second()))
                    throw new RuntimeException("Cannot build a road not connected to a town");
                tryBuildTown(player, p.first());
                tryBuildRoad(player, p.second());
            }
        }

        turnNumber = 0;
        whichPlayerTurn = -1;

        while (true) {
            turnNumber++;
            whichPlayerTurn = (whichPlayerTurn + 1) % players.size();

            players.get(whichPlayerTurn).bot().makeTurn();

        }
    }

    void tryBuildTown(Player player, Board.Intersection i, boolean mustBeRoad) {
        if (!board.canBuildTownAt(i)) // TODO: rename method
            throw new RuntimeException("Cannot build a town here");
        if (mustBeRoad) {
            // TODO: check for roads
        }
        board.buildTown(i, new Town(player, false));
    }

    void tryBuildRoad(Player player, Board.Path p) {
        // TODO: check a road nearby
        board.buildRoad(p, player);
    }

    Player player(Bot bot) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).bot().equals(bot)) {
                return players.get(i);
            }
        }
        throw new IllegalStateException("Internal: no player corresponds to the given bot");
    }

}

