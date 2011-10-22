package settlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import settlers.bot.Bot;
import settlers.util.Pair;
import settlers.util.Util;

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
        public DevelopmentStack developments() { return player(bot).developments(); }

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

        public boolean havePort(Resource r)
            { return game.hasPort(r, player(bot)); }
        public boolean havePort3to1()
            { return game.hasPort3to1(player(bot)); }
        public boolean hasPort(Resource r, Player player)
            { return game.hasPort(r, player); }
        public boolean hasPort3to1(Player player)
            { return game.hasPort3to1(player); }
        public boolean canChange(String sell, String buy)
            { return game.canChange(sell, buy, player(bot)); }
        public void change(String sell, String buy)
            { game.change(sell, buy, player(bot)); }

        public boolean getIfPossible(String what)
            { return game.getIfPossible(what, player(bot)); }

        public int developmentsLeft()
            { return game.developments.size(); }
        public void drawDevelopment()
            { game.drawDevelopment(player(bot)); }
        public void monopoly(Resource r)
            { game.monopoly(r, player(bot)); }
        public void roadBuilding(Board.Path p1, Board.Path p2)
            { game.roadBuilding(p1, p2, player(bot)); }
        public void invention(Resource r1, Resource r2)
            { game.invention(r1, r2, player(bot)); }
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
    private List<Development> developments = new ArrayList<Development>();

    Game() {
        board = Board.create(rnd);
        for (int i = 0; i < 14; i++)
            developments.add(Development.KNIGHT);
        for (int i = 0; i < 2; i++) {
            developments.add(Development.ROAD_BUILDING);
            developments.add(Development.INVENTION);
            developments.add(Development.MONOPOLY);
        }
        for (int i = 0; i < 5; i++)
            developments.add(Development.VICTORY_POINT);
        Collections.shuffle(developments, rnd);
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
System.out.println("Dice rolled: " + diceRolled + " (" + players.get(whichPlayerTurn).color() + ")");
        if (diceRolled == 7) {
            for (Player p : players) {
                int were = p.cards().size();
                if (were > 7) {
                    List<Resource> discard = p.bot().discardHalfOfTheCards();
System.out.print(p.color() + " discards: ");
for (Resource r : discard) System.out.print(r.chr());
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
for (Player p : players) System.out.println(p.color() + ": " + p.cards() + " " + p.developments());
        return diceRolled;
    }

    void moveRobber(Board.Cell cell, Player player, Player whoToRob) {
        if (cell == board.robber())
            throw new RuntimeException("You cannot leave the robber at his current position");
        if (whoToRob == player)
            throw new RuntimeException("You cannot rob yourself");
        List<Town> ts = board.adjacentTowns(cell);
        List<Player> okToRob = new ArrayList<Player>();
        for (Town t : ts)
            if (t.player().cardsNumber() > 0)
                okToRob.add(t.player());
        if (!okToRob.isEmpty() && whoToRob == null)
            throw new RuntimeException("You must rob somebody");
        if (!okToRob.isEmpty() && !okToRob.contains(whoToRob))
            throw new RuntimeException("You cannot rob a player not having a town near the robber");
        board.moveRobber(cell);
        robberMoved = true;
if (whoToRob == null) System.out.println(player.color() + " moves robber to " + cell + " and robs nobody");
        if (whoToRob == null)
            return;
        if (whoToRob.cardsNumber() == 0)
            throw new RuntimeException("You cannot rob a player who has no cards");
        List<Resource> list = whoToRob.cards().list();
        Resource r = list.get(rnd.nextInt(list.size()));
        whoToRob.cards().sub(r, 1);
        player.cards().add(r, 1);
System.out.println(player.color() + " moves robber to " + cell + " and robs " + whoToRob.color());
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
        player.cards().sub("BWGL");
System.out.println(player.color() + " builds a settlement at " + i);
    }

    void buildCity(Board.Intersection i, Player player) {
        if (player.citiesLeft() == 0)
            throw new RuntimeException("You do not have any cities left");
        if (!player.cards().areThere("OOOGG"))
            throw new RuntimeException("Not enough resources to build a city");
        if (board.townAt(i) == null)
            throw new RuntimeException("You must first build a settlement to be able to upgrade it");
        if (board.townAt(i).isCity())
            throw new RuntimeException("You cannot build a city over an existing city");
        if (board.townAt(i).player() != player)
            throw new RuntimeException("You cannot upgrade other player's settlement");
        player.expendCity();
        board.buildTown(i, new Town(player, true));
        player.cards().sub("OOOGG");
System.out.println(player.color() + " builds a city at " + i);
    }

    void buildRoad(Board.Path p, Player player) {
        if (player.roadsLeft() == 0)
            throw new RuntimeException("You do not have any roads left");
        if (!player.cards().areThere("BL"))
            throw new RuntimeException("Not enough resources to build a road");
        if (!board.canBuildRoadAt(p, player))
            throw new RuntimeException("You cannot build a road here");
        player.expendRoad();
        board.buildRoad(p, player);
        player.cards().sub("BL");
System.out.println(player.color() + " builds a road at " + p);
    }

    boolean hasPort(Resource r, Player player) {
        for (Pair<Board.Intersection, Resource> p : board.allPorts()) {
            if (p.second() == r) {
                Town t = board.townAt(p.first());
                if (t != null && t.player() == player)
                    return true;
            }
        }
        return false;
    }

    boolean hasPort3to1(Player player) {
        for (Pair<Board.Intersection, Resource> p : board.allPorts()) {
            if (p.second() == null) {
                Town t = board.townAt(p.first());
                if (t != null && t.player() == player)
                    return true;
            }
        }
        return false;
    }

    boolean canChange(String sell, String buy, Player player) {
        int res = 0;
        for (Resource r : Resource.all()) {
            int rsell = Util.numberOfOccurrences(r.chr(), sell);
            int rbuy = Util.numberOfOccurrences(r.chr(), buy);
            int min = Math.min(rsell, rbuy);
            rsell -= min;
            rbuy -= min;
            if (rbuy > 0) {
                res -= rbuy;
                continue;
            }
            if (hasPort(r, player)) {
                if (rsell % 2 != 0)
                    return false;
                res += rsell / 2;
            } else if (hasPort3to1(player)) {
                if (rsell % 3 != 0)
                    return false;
                res += rsell / 3;
            } else {
                if (rsell % 4 != 0)
                    return false;
                res += rsell / 4;
            }
        }
        return res == 0;
    }

    void change(String sell, String buy, Player player) {
        if (!canChange(sell, buy, player))
            throw new RuntimeException("You cannot change " + sell + " to " + buy);
        player.cards().sub(sell);
        player.cards().add(buy);
System.out.println(player.color() + " changes " + sell + " to " + buy);
    }

    boolean getIfPossible(String what, Player player) {
        Map<Resource, Integer> left = new HashMap<Resource, Integer>();
        String buy = "";
        for (Resource r : Resource.all()) {
            int needed = Util.numberOfOccurrences(r.chr(), what);
            int has = player.cards().howMany(r);
            if (has >= needed) {
                left.put(r, has - needed);
            } else {
                left.put(r, 0);
                for (int i = has; i < needed; i++)
                    buy += r.chr();
            }
        }
        if (buy.isEmpty())
            return true;
        String sell = "";
        int buyingIndex = 0;
        // first 2:1, then 3:1 or 4:1
        it: for (int it = 0; it < 2; it++) {
            for (Resource r : Resource.all()) {
                if (it == 0 && !hasPort(r, player))
                    continue;
                int x = left.get(r);
                if (x == 0)
                    continue;
                int coeff = it == 0 ? 2 : 4;
                if (hasPort3to1(player))
                    coeff = 3;
                int sub = Math.min(buy.length() - buyingIndex, x / coeff);
                for (int i = 0; i < sub; i++)
                    for (int j = 0; j < coeff; j++)
                        sell += r.chr();
                buyingIndex += sub;
                if (buyingIndex == buy.length())
                    break it;
                left.put(r, x - sub * coeff);
            }
        }
        if (buyingIndex < buy.length())
            return false;
        change(sell, buy, player);
        return true;
    }

    void drawDevelopment(Player player) {
        if (developments.isEmpty())
            throw new RuntimeException("No more developments left in the game");
        if (!player.cards().areThere("WOG"))
            throw new RuntimeException("Not enough resources to draw a development");
        Development d = developments.remove(developments.size() - 1);
        player.developments().add(d);
        player.cards().sub("WOG");
System.out.println(player.color() + " draws a development");
System.out.println("(it's " + d + ")");
    }

    void monopoly(Resource r, Player player) {
        player.developments().use(Development.MONOPOLY);
int sum = 0;
        for (Player p : players) {
            if (p == player)
                continue;
            int x = p.cards().howMany(r);
sum += x;
            player.cards().add(r, x);
            p.cards().sub(r, x);
        }
System.out.println(player.color() + " declared monopoly and received " + sum + " of " + r);
    }

    void roadBuilding(Board.Path p1, Board.Path p2, Player player) {
        player.developments().use(Development.ROAD_BUILDING);
        if (player.roadsLeft() == 0)
            throw new RuntimeException("You do not have any roads left");
        if (player.roadsLeft() == 1) {
            if (p1 == null) { Board.Path p = p1; p1 = p2; p2 = p; }
            if (p2 != null)
                throw new RuntimeException("You have only 1 road left");
        }
        if (!board.canBuildRoadAt(p1, player))
            throw new RuntimeException("You cannot build a road here");
        player.expendRoad();
        board.buildRoad(p1, player);
        if (p2 != null) {
            if (!board.canBuildRoadAt(p2, player))
                throw new RuntimeException("You cannot build a road here");
            player.expendRoad();
            board.buildRoad(p2, player);
        }
System.out.println(player.color() + " plays road building and builds roads at " + p1 + " and " + p2);
    }

    void invention(Resource r1, Resource r2, Player player) {
        player.developments().use(Development.INVENTION);
        player.cards().add(r1, 1);
        player.cards().add(r2, 1);
System.out.println(player.color() + " plays invention and receives " + r1 + " and " + r2);
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
            Player player = players.get(whichPlayerTurn);

            player.bot().makeTurn();

            player.developments().reenable();
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
        for (Player player : players) {
            System.out.print(points(player) + " " + player.color() + " (" + player.bot() + ")");
            int vp = player.developments().victoryPoint();
            System.out.println(vp > 0 ? " (" + vp + " VP)" : "");
        }
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

