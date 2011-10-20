package settlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import settlers.bot.Bot;
import settlers.util.Pair;

public class Game {
    
    public class API {
        private final Game game;
        private Bot bot;

        API() { game = Game.this; }

        public Game game() { return game; }
        public Random rnd() { return game.rnd; }
        void setBot(Bot bot) { this.bot = bot; }

        public CardStack cards() { return game.cards[index(bot)]; }
        public int rollDice() { return game.rollDice(); }
    }

    private final Random rnd = new Random();
    
    private final List<Player> players = new ArrayList<Player>();
    private final Board board;

    private int n;
    private int turnNumber;
    private int whichPlayerTurn;
    private int diceRolled;

    private int[] armyStrength;
    private Player largestArmy;
    private Player longestRoad;
    private CardStack[] cards;
    private DevelopmentStack[] developments;

    Game() {
        board = Board.create(rnd);
    }

    public List<Player> players() { return Collections.unmodifiableList(players); }
    public Board board() { return board; }
    
    public int turnNumber() { return turnNumber; }
    public int whichPlayerTurn() { return whichPlayerTurn; }


    TradeOffer createTradeOffer(
        Bot bot,
        Map<Resource, Integer> iWant,
        Map<Resource, Integer> iGive
    ) {
        return new TradeOffer(player(bot), iWant, iGive);
    }


    int rollDice() {
        if (diceRolled != 0)
            throw new RuntimeException("Cannot roll the dice twice a turn");
        diceRolled = rnd.nextInt(6) + rnd.nextInt(6) + 2;
        if (diceRolled == 7) {
            for (int i = 0; i < n; i++) {
                int were = cards[i].size();
                if (were > 7) {
                    List<Resource> discard = players.get(i).bot().discardHalfOfTheCards();
                    for (Resource r : discard)
                        cards[i].sub(r, 1);
                    if (cards[i].size() != (were + 1) / 2)
                        throw new RuntimeException("You must discard half of your cards");
                }
            }
        } else {
            for (Board.Cell cell : Board.allCells()) {
                if (board.numberAt(cell) != diceRolled)
                    continue;
                if (board.robberAt() == cell)
                    continue;
                for (Board.Intersection ints : Board.adjacentIntersections(cell)) {
                    Town town = board.townAt(ints);
                    if (town == null)
                        continue;
                    cards[index(town.player())].add(
                        board.resourceAt(cell),
                        town.isCity() ? 2 : 1
                    );
                }
            }
        }
        return diceRolled;
    }



    int roadLength(Player player) {
        // TODO
        return 1;
    }

    void addPlayer(Player player) {
        players.add(player);
    }

    void play() {
        init();
        placeFirstSettlements();

        turnNumber = 0;
        whichPlayerTurn = -1;

        while (true) {
            turnNumber++;
            whichPlayerTurn = (whichPlayerTurn + 1) % n;
            diceRolled = 0;

            players.get(whichPlayerTurn).bot().makeTurn();

            updateLongestRoad();
            updateLargestArmy();

            if (turnNumber > 10000) break;
            // if (playerHasWon()) break;
        }

        System.out.println("Winner: " + players.get(whichPlayerTurn).color() + ", " +
            players.get(whichPlayerTurn).bot().toString());
    }

    void init() {
        n = players.size();
        Collections.shuffle(players, rnd);
        cards = new CardStack[n];
        developments = new DevelopmentStack[n];
        armyStrength = new int[n];
        for (int i = 0; i < n; i++) {
            cards[i] = new CardStack();
            developments[i] = new DevelopmentStack();
        }
    }

    void placeFirstSettlements() {
        for (int it = 0; it < 2; it++) {
            for (int i = it * (n - 1); 0 <= i && i < n; i += 1 - 2*it) {
                Player player = players.get(i);
                Pair<Board.Intersection, Board.Path> p = player.bot().placeFirstSettlements(it == 0);
                if (!board.areAdjacent(p.first(), p.second()))
                    throw new RuntimeException("Cannot build a road not connected to a town");
                tryBuildTown(player, p.first(), false);
                tryBuildRoad(player, p.second());
            }
        }
    }

    void tryBuildTown(Player player, Board.Intersection i, boolean mustBeRoad) {
        if (!board.canBuildTownAt(i))
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

    boolean playerHasWon() {
        Player player = players.get(whichPlayerTurn);
        int points = 0;
        for (Pair<Board.Intersection, Town> t : board.allTowns())
            points += t.second().isCity() ? 2 : 1;
        if (longestRoad == player)
            points += 2;
        if (largestArmy == player)
            points += 2;
        points += developments[whichPlayerTurn].victoryPoint();
        return points >= 10;
    }

    void updateLongestRoad() {
        Player player = players.get(whichPlayerTurn);
        int z = roadLength(player);
        for (Player rival : players)
            if (rival != player && roadLength(rival) >= z)
                return;
        longestRoad = player;
    }

    void updateLargestArmy() {
        int z = armyStrength[whichPlayerTurn];
        for (int i = 0; i < n; i++)
            if (i != whichPlayerTurn && armyStrength[i] >= z)
                return;
        largestArmy = players.get(whichPlayerTurn);
    }

    int index(Player player) {
        int ans = players.indexOf(player);
        if (ans < 0)
            throw new IllegalStateException("Internal: player not found");
        return ans;
    }

    int index(Bot bot) {
        for (int i = 0; i < n; i++)
            if (players.get(i).bot() == bot)
                return i;
        throw new IllegalStateException("Internal: bot not found");
    }

    Player player(Bot bot) {
        for (Player player : players)
            if (player.bot() == bot)
                return player;
        throw new IllegalStateException("Internal: no player corresponds to the given bot");
    }

}

