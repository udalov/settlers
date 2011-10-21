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
        public void moveRobber(Board.Cell c, Player whoToRob)
            { game.moveRobber(c, player(bot), whoToRob); }

        public boolean canBuildFirstTownsAt(Board.Intersection i)
            { return game.board.canBuildTownAt(i, false, player(bot)); }
        public boolean canBuildTownAt(Board.Intersection i)
            { return game.board.canBuildTownAt(i, true, player(bot)); }
        public boolean canBuildRoadAt(Board.Path p)
            { return game.board.canBuildRoadAt(p, player(bot)); }

        public void buildSettlement(Board.Intersection i)
            { game.buildSettlement(i, player(bot)); }
        public void buildCity(Board.Intersection i)
            { game.buildCity(i, player(bot)); }
        public void buildRoad(Board.Path p)
            { game.buildRoad(p, player(bot)); }
    }

    private final Random rnd = new Random(256);
    
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
for (Player p : players) System.out.println(p.color() + ": " + p.cards());
System.out.println("Dice rolled: " + diceRolled + " (" + players.get(whichPlayerTurn).color() + ")");
        if (diceRolled == 7) {
            for (Player p : players) {
                int were = p.cards().size();
                if (were > 7) {
                    List<Resource> discard = p.bot().discardHalfOfTheCards();
System.out.println(p.color() + " discards: ");
for (Resource r : discard) System.out.print(r.toString().charAt(0));
System.out.println();
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

    void moveRobber(Board.Cell cell, Player player, Player whoToRob) {
        if (cell == board.robber())
            throw new RuntimeException("You cannot leave the robber at his current position");
        List<Town> ts = board.adjacentTowns(cell);
        List<Player> okToRob = new ArrayList<Player>();
        for (Town t : ts)
            if (t.player().cardsNumber() > 0)
                okToRob.add(t.player());
        if (!okToRob.isEmpty() && whoToRob == null)
            throw new RuntimeException("You must rob somebody");
        if (!okToRob.isEmpty() && !okToRob.contains(whoToRob))
            throw new RuntimeException("You cannot rob a player not having a town near the robber");
        if (whoToRob.cardsNumber() == 0)
            throw new RuntimeException("You cannot rob a player who has no cards");
        board.moveRobber(cell);
        List<Resource> list = whoToRob.cards().list();
        Resource r = list.get(rnd.nextInt(list.size()));
        whoToRob.cards().sub(r, 1);
        player.cards().add(r, 1);
        robberMoved = true;
System.out.println(player.color() + " moves robber to " + cell + " and robbes " + (whoToRob == null ? "nobody" : whoToRob.color().toString()));
    }

    void buildSettlement(Board.Intersection i, Player player) {
        if (player.settlementsLeft() == 0)
            throw new RuntimeException("You do not have any settlements left");
        if (!player.cards().areThere("BWGL"))
            throw new RuntimeException("Not enough resources to build a settlement");
        if (!board.canBuildTownAt(i, true, player))
            throw new RuntimeException("You cannot build a settlement here");
        player.expendSettlement();
        board.buildTown(i, new Town(player, false));
        player.cards().sub(Resource.BRICK, 1);
        player.cards().sub(Resource.WOOL, 1);
        player.cards().sub(Resource.GRAIN, 1);
        player.cards().sub(Resource.LUMBER, 1);
System.out.println(player.color() + " builds a settlement at " + i);
    }

    void buildCity(Board.Intersection i, Player player) {
        if (player.citiesLeft() == 0)
            throw new RuntimeException("You do not have any cities left");
        if (!player.cards().areThere("OOOGG"))
            throw new RuntimeException("Not enough resources to build a city");
        if (board.townAt(i) == null)
            throw new RuntimeException("You must first build a settlement to be able to upgrade it");
        if (board.townAt(i).player() != player)
            throw new RuntimeException("You cannot upgrade other player's settlement");
        if (board.townAt(i).isCity())
            throw new RuntimeException("You cannot build a city over your city");
        player.expendCity();
        board.buildTown(i, new Town(player, true));
        player.cards().sub(Resource.ORE, 1);
        player.cards().sub(Resource.ORE, 1);
        player.cards().sub(Resource.ORE, 1);
        player.cards().sub(Resource.GRAIN, 1);
        player.cards().sub(Resource.GRAIN, 1);
System.out.println(player.color() + " builds a city at " + i);
    }

    void buildRoad(Board.Path p, Player player) {
        if (player.roadsLeft() == 0)
            throw new RuntimeException("You do not have any roads left");
        if (!board.canBuildRoadAt(p, player))
            throw new RuntimeException("You cannot build a road here");
        player.expendRoad();
        board.buildRoad(p, player);
        player.cards().sub(Resource.BRICK, 1);
        player.cards().sub(Resource.LUMBER, 1);
System.out.println(player.color() + " builds a road at " + p);
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
System.out.println();
            turnNumber++;
            whichPlayerTurn = (whichPlayerTurn + 1) % n;
            diceRolled = 0;
            robberMoved = false;

            players.get(whichPlayerTurn).bot().makeTurn();

            updateLongestRoad();
            updateLargestArmy();

            if (playerHasWon()) break;

            if (diceRolled == 0)
                throw new RuntimeException("You must roll the dice once a turn");
            if (diceRolled == 7 && !robberMoved)
                throw new RuntimeException("You must move the robber if you rolled 7");
        }

        // TODO: do not output VP for players who lost
        System.out.println();
        System.out.println("Game lasted " + turnNumber + " turns");
        System.out.println("Scores:");
        for (Player player : players)
            System.out.println(points(player) + " " + player.color() + " (" + player.bot() + ")");
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
                if (!board.canBuildTownAt(p.first(), false, player))
                    throw new RuntimeException("Cannot build a town here");
                board.buildTown(p.first(), new Town(player, false));
                board.buildRoad(p.second(), player);
                if (it == 1) {
                    for (Board.Cell c : Board.adjacentCells(p.first())) {
                        Resource r = board.resourceAt(c);
                        if (r != null)
                            player.cards().add(r, 1);
                    }
                }
            }
        }
    }

    int points(Player player) {
        int points = 0;
        for (Pair<Board.Intersection, Town> pair : board.allTowns())
            if (pair.second().player() == player)
                points += pair.second().isCity() ? 2 : 1;
        if (longestRoad == player)
            points += 2;
        if (largestArmy == player)
            points += 2;
        points += player.developments().victoryPoint();
        return points;
    }

    boolean playerHasWon() {
        return points(players.get(whichPlayerTurn)) >= 10;
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

