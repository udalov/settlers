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
        void setBot(Bot bot) { this.bot = bot; }

        public Game game() { return game; }
        public Player me() { return player(bot); }
        public Random rnd() { return game.rnd; }
        public Board board() { return game.board; }
        public List<Player> players() { return game.players(); }
        public int turnNumber() { return turnNumber; }
        public int whichPlayerTurn() { return whichPlayerTurn; }

        public CardStack cards() { return player(bot).cards(); }
        public int rollDice() { return game.rollDice(); }
        public void moveRobber(Board.Cell c, Player p) { game.moveRobber(c, p); }
    }

    private final Random rnd = new Random();
    
    private final List<Player> players = new ArrayList<Player>();
    private final Board board;

    private int n;
    private int turnNumber;
    private int whichPlayerTurn;
    private int diceRolled;
    private boolean robberMoved;

    private Player largestArmy;
    private Player longestRoad;

    Game() {
        board = Board.create(rnd);
    }

    List<Player> players() { return Collections.unmodifiableList(players); }
    public Board board() { return board; }

    int turnNumber() { return turnNumber; }
    int whichPlayerTurn() { return whichPlayerTurn; }


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
            for (Player p : players) {
                int were = p.cards().size();
                if (were > 7) {
                    List<Resource> discard = p.bot().discardHalfOfTheCards();
                    for (Resource r : discard)
                        p.cards().sub(r, 1);
                    if (p.cards().size() != (were + 1) / 2)
                        throw new RuntimeException("You must discard half of your cards");
                }
            }
        } else {
            for (Board.Cell cell : Board.allCells()) {
                if (board.numberAt(cell) != diceRolled)
                    continue;
                if (board.robber() == cell)
                    continue;
                for (Board.Intersection ints : Board.adjacentIntersections(cell)) {
                    Town town = board.townAt(ints);
                    if (town == null)
                        continue;
                    town.player().cards().add(
                        board.resourceAt(cell),
                        town.isCity() ? 2 : 1
                    );
                }
            }
        }
        return diceRolled;
    }

    void moveRobber(Board.Cell cell, Player player) {
        if (cell == board.robber())
            throw new RuntimeException("You cannot leave the robber at his current position");
        List<Town> ts = board.adjacentTowns(cell);
        boolean ok = false;
        for (Town t : ts)
            ok |= t.player() == player;
        if (!ok)
            throw new RuntimeException("You cannot rob a player not having a town near the robber");
        if (player.cardsNumber() == 0)
            throw new RuntimeException("You cannot rob a player who has no cards");
        board.moveRobber(cell);
        List<Resource> list = player.cards().list();
        Resource r = list.get(rnd.nextInt(list.size()));
        player.cards().sub(r, 1);
        players.get(whichPlayerTurn).cards().add(r, 1);
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
            robberMoved = false;

            players.get(whichPlayerTurn).bot().makeTurn();

            updateLongestRoad();
            updateLargestArmy();

if (turnNumber > 10000) break;
            // if (playerHasWon()) break;

            if (diceRolled == 0)
                throw new RuntimeException("You must roll the dice once a turn");
            if (diceRolled == 7 && !robberMoved)
                throw new RuntimeException("You must move the robber if you rolled 7");
        }

        System.out.println("Winner: " + players.get(whichPlayerTurn).color() + ", " +
            players.get(whichPlayerTurn).bot().toString());
    }

    void init() {
        n = players.size();
        Collections.shuffle(players, rnd);
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
        points += player.developments().victoryPoint();
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
        Player player = players.get(whichPlayerTurn);
        int z = player.armyStrength();
        for (Player p : players)
            if (p != player && p.armyStrength() >= z)
                return;
        largestArmy = player;
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
        return null;
    }

}

