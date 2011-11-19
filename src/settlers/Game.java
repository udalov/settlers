package settlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import settlers.bot.Bot;
import settlers.util.Pair;
import settlers.util.Util;

public class Game {
    
    public class API {
        private final Game game;
        private Bot bot;

        API() { game = Game.this; }
        void setBot(Bot bot) { this.bot = bot; }

        void checkTurn() {
            if (player(bot) != turn)
                throw new RuntimeException("You cannot do anything not on your turn");
        }
        void checkRobber() {
            if (diceRolled == 7 && !robberMoved)
                throw new RuntimeException("You must move the robber right after rolling 7");
        }
        void check() {
            checkTurn();
            checkRobber();
        }

        public Game game() { return game; }
        public Player me() { return player(bot); }
        public Random rnd() { return game.rnd; }
        public Board board() { return game.board; }
        public List<Player> players() { return game.players(); }
        public int turnNumber() { return turnNumber; }
        public Player turn() { return turn; }

        public CardStack cards() { return player(bot).cards(); }
        public DevelopmentStack developments() { return player(bot).developments(); }
        public CardStack bank() { return game.bank; }

        public int rollDice() { check(); return game.rollDice(); }

        public void moveRobber(Hex c, Player whoToRob)
            { checkTurn(); game.moveRobber(c, whoToRob); }

        public boolean canBuildTownAt(Xing i, boolean mustBeRoad)
            { return game.board.canBuildTownAt(i, mustBeRoad, me()); }
        public boolean canBuildRoadAt(Path p)
            { return game.board.canBuildRoadAt(p, me()); }

        public void buildSettlement(Xing i)
            { check(); game.buildSettlement(i); }
        public void buildCity(Xing i)
            { check(); game.buildCity(i); }
        public void buildRoad(Path p)
            { check(); game.buildRoad(p); }

        public boolean havePort(Resource r)
            { return game.hasPort(r, me()); }
        public boolean havePort3to1()
            { return game.hasPort3to1(me()); }
        public boolean hasPort(Resource r, Player player)
            { return game.hasPort(r, player); }
        public boolean hasPort3to1(Player player)
            { return game.hasPort3to1(player); }
        public boolean canChange(String sell, String buy)
            { return game.canChange(sell, buy, me()); }
        public void change(String sell, String buy)
            { check(); game.change(sell, buy); }

        public boolean getIfPossible(String what)
            { check(); return game.getIfPossible(what); }

        public int developmentsLeft()
            { return game.developments.size(); }
        public void drawDevelopment()
            { check(); game.drawDevelopment(); }
        public void monopoly(Resource r)
            { check(); game.monopoly(r); }
        public void roadBuilding(Path p1, Path p2)
            { check(); game.roadBuilding(p1, p2); }
        public void invention(Resource r1, Resource r2)
            { check(); game.invention(r1, r2); }
        public void knight(Hex hex, Player whoToRob)
            { check(); game.knight(hex, whoToRob); }

        public Player largestArmy()
            { return game.largestArmy; }
        public int roadLength(Player player)
            { return game.roadLength(player); }
        public int roadLengthWith(Player player, Path p)
            { return game.roadLengthWith(player, p); }

        public List<TradeResult> trade(String sell, String buy)
            { check(); return game.trade(new TradeOffer(me(), sell, buy)); }
        public TradeResult acceptOffer(TradeOffer offer)
            { check(); return offer.accept(me()); }
        public TradeResult declineOffer(TradeOffer offer)
            { check(); return offer.decline(me()); }
        public TradeResult counterOffer(TradeOffer offer, String sell, String buy)
            { check(); return offer.counteroffer(new TradeOffer(me(), sell, buy)); }
    }

    private final Random rnd = new Random(256);
    
    private final List<Player> players = new ArrayList<Player>();
    private final Board board;

    private int n;
    private int turnNumber;
    private Player turn;
    private int diceRolled;
    private boolean robberMoved;
    private final CardStack bank = new CardStack();

    private Player largestArmy;
    private Player longestRoad;
    private final List<Development> developments = new ArrayList<Development>();

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
        for (Resource r : Resource.all())
            bank.add(r, 19);
    }

    List<Player> players() { return Collections.unmodifiableList(players); }
    public Board board() { return board; }

    int turnNumber() { return turnNumber; }



    List<TradeResult> trade(TradeOffer offer) {
        List<TradeResult> ans = new ArrayList<TradeResult>();
        for (Player p : players)
            if (p != offer.trader())
                ans.add(p.bot().trade(offer));
        return ans;
    }

    int rollDice() {
        if (diceRolled != 0)
            throw new RuntimeException("Cannot roll the dice twice a turn");
        diceRolled = rnd.nextInt(6) + rnd.nextInt(6) + 2;
System.out.println("Dice rolled: " + diceRolled + " (" + turn.color() + ")");
        if (diceRolled == 7) {
            for (Player p : players) {
                int were = p.cards().size();
                if (were > 7) {
                    List<Resource> discard = p.bot().discardHalfOfTheCards();
System.out.print(p.color() + " discards: ");
for (Resource r : discard) System.out.print(r.chr());
System.out.println();
                    for (Resource r : discard) {
                        p.cards().sub(r, 1);
                        bank.add(r, 1);
                    }
                    if (p.cards().size() != (were + 1) / 2)
                        throw new RuntimeException("You must discard half of your cards");
                }
            }
        } else {
            int[] neededResCards = new int[Resource.all().length];
            for (int step = 0; step < 2; step++) {
                for (Hex hex : Board.allHexes()) {
                    if (board.numberAt(hex) != diceRolled)
                        continue;
                    if (board.robber() == hex)
                        continue;
                    for (Xing x : Board.adjacentXings(hex)) {
                        Town town = board.townAt(x);
                        if (town == null)
                            continue;
                        Resource res = board.resourceAt(hex);
                        int q = town.isCity() ? 2 : 1;
                        int index = res.index();
                        if (step == 0) {
                            neededResCards[index] += q;
                        } else if (neededResCards[index] <= bank.howMany(res)) {
                            town.player().cards().add(res, q);
                            bank.sub(res, q);
                        }
                    }
                }
            }
        }
for (Player p : players) System.out.println(p.color() + ": " + p.cards() + " " + p.developments());
        return diceRolled;
    }

    void moveRobber(Hex hex, Player whoToRob) {
        if (hex == board.robber())
            throw new RuntimeException("You cannot leave the robber at his current position");
        if (whoToRob == turn)
            throw new RuntimeException("You cannot rob yourself");
        List<Town> ts = board.adjacentTowns(hex);
        List<Player> okToRob = new ArrayList<Player>();
        for (Town t : ts)
            if (t.player().cardsNumber() > 0)
                okToRob.add(t.player());
        if (!okToRob.isEmpty() && whoToRob == null)
            throw new RuntimeException("You must rob somebody");
        if (!okToRob.isEmpty() && !okToRob.contains(whoToRob))
            throw new RuntimeException("You cannot rob a player not having a town near the robber");
        board.moveRobber(hex);
        robberMoved = true;
if (whoToRob == null) System.out.println(turn.color() + " moves robber to " + hex + " and robs nobody");
        if (whoToRob == null)
            return;
        if (whoToRob.cardsNumber() == 0)
            throw new RuntimeException("You cannot rob a player who has no cards");
        List<Resource> list = whoToRob.cards().list();
        Resource r = list.get(rnd.nextInt(list.size()));
        whoToRob.cards().sub(r, 1);
        turn.cards().add(r, 1);
System.out.println(turn.color() + " moves robber to " + hex + " and robs " + whoToRob.color());
    }

    void buildSettlement(Xing i) {
        if (turn.settlementsLeft() == 0)
            throw new RuntimeException("You do not have any settlements left");
        if (!turn.cards().areThere("BWGL"))
            throw new RuntimeException("Not enough resources to build a settlement");
        if (!board.canBuildTownAt(i, true, turn))
            throw new RuntimeException("You cannot build a settlement here");
        turn.expendSettlement();
        board.buildTown(i, new Town(turn, false));
        turn.cards().sub("BWGL");
        bank.add("BWGL");
System.out.println(turn.color() + " builds a settlement at " + i);
    }

    void buildCity(Xing i) {
        if (turn.citiesLeft() == 0)
            throw new RuntimeException("You do not have any cities left");
        if (!turn.cards().areThere("OOOGG"))
            throw new RuntimeException("Not enough resources to build a city");
        if (board.townAt(i) == null)
            throw new RuntimeException("You must first build a settlement to be able to upgrade it");
        if (board.townAt(i).isCity())
            throw new RuntimeException("You cannot build a city over an existing city");
        if (board.townAt(i).player() != turn)
            throw new RuntimeException("You cannot upgrade other player's settlement");
        turn.expendCity();
        board.buildTown(i, new Town(turn, true));
        turn.cards().sub("OOOGG");
        bank.add("OOOGG");
System.out.println(turn.color() + " builds a city at " + i);
    }

    void buildRoad(Path p) {
        if (turn.roadsLeft() == 0)
            throw new RuntimeException("You do not have any roads left");
        if (!turn.cards().areThere("BL"))
            throw new RuntimeException("Not enough resources to build a road");
        if (!board.canBuildRoadAt(p, turn))
            throw new RuntimeException("You cannot build a road here");
        turn.expendRoad();
        board.buildRoad(p, turn);
        turn.cards().sub("BL");
        bank.add("BL");
System.out.println(turn.color() + " builds a road at " + p);
    }

    boolean hasPort(Resource r, Player player) {
        for (Pair<Xing, Resource> p : board.allPorts()) {
            if (p.second() == r) {
                Town t = board.townAt(p.first());
                if (t != null && t.player() == player)
                    return true;
            }
        }
        return false;
    }

    boolean hasPort3to1(Player player) {
        for (Pair<Xing, Resource> p : board.allPorts()) {
            if (p.second() == null) {
                Town t = board.townAt(p.first());
                if (t != null && t.player() == player)
                    return true;
            }
        }
        return false;
    }

    boolean canChange(String sell, String buy, Player player) {
        if (!bank.areThere(buy))
            return false;
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

    void change(String sell, String buy) {
        if (!canChange(sell, buy, turn))
            throw new RuntimeException("You cannot change " + sell + " to " + buy);
        turn.cards().sub(sell);
        turn.cards().add(buy);
        bank.add(sell);
        bank.sub(buy);
System.out.println(turn.color() + " changes " + sell + " to " + buy);
    }

    boolean getIfPossible(String what) {
        Map<Resource, Integer> left = new HashMap<Resource, Integer>();
        String buy = "";
        for (Resource r : Resource.all()) {
            int needed = Util.numberOfOccurrences(r.chr(), what);
            int has = turn.cards().howMany(r);
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
                if (it == 0 && !hasPort(r, turn))
                    continue;
                int x = left.get(r);
                if (x == 0)
                    continue;
                int coeff = it == 0 ? 2 : hasPort3to1(turn) ? 3 : 4;
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
        change(sell, buy);
        return true;
    }

    void drawDevelopment() {
        if (developments.isEmpty())
            throw new RuntimeException("No more developments left in the game");
        if (!turn.cards().areThere("WOG"))
            throw new RuntimeException("Not enough resources to draw a development");
        Development d = developments.remove(developments.size() - 1);
        turn.developments().add(d);
        turn.cards().sub("WOG");
        bank.add("WOG");
System.out.println(turn.color() + " draws a development");
System.out.println("(it's " + d + ")");
    }

    void monopoly(Resource r) {
        turn.developments().use(Development.MONOPOLY);
int sum = 0;
        for (Player p : players) {
            if (p == turn)
                continue;
            int x = p.cards().howMany(r);
sum += x;
            turn.cards().add(r, x);
            p.cards().sub(r, x);
        }
System.out.println(turn.color() + " declares monopoly and receives " + sum + " of " + r);
    }

    void roadBuilding(Path p1, Path p2) {
        turn.developments().use(Development.ROAD_BUILDING);
        if (turn.roadsLeft() == 0)
            throw new RuntimeException("You do not have any roads left to use road building card");
        if (turn.roadsLeft() == 1) {
            if (p1 == null) { Path p = p1; p1 = p2; p2 = p; }
            if (p2 != null)
                throw new RuntimeException("You have only 1 road left to use road building card");
        }
        if (!board.canBuildRoadAt(p1, turn))
            throw new RuntimeException("You cannot build a road here");
        turn.expendRoad();
        board.buildRoad(p1, turn);
        if (p2 != null) {
            if (!board.canBuildRoadAt(p2, turn))
                throw new RuntimeException("You cannot build a road here");
            turn.expendRoad();
            board.buildRoad(p2, turn);
        }
System.out.println(turn.color() + " plays road building and builds roads at " + p1 + " and " + p2);
    }

    void invention(Resource r1, Resource r2) {
        if (bank.howMany(r1) == 0 || bank.howMany(r2) == 0)
            throw new RuntimeException("You cannot use invention card on non-existing resources");
        turn.developments().use(Development.INVENTION);
        turn.cards().add(r1, 1);
        turn.cards().add(r2, 1);
        bank.sub(r1, 1);
        bank.sub(r2, 1);
System.out.println(turn.color() + " plays invention and receives " + r1 + " and " + r2);
    }

    void knight(Hex hex, Player whoToRob) {
        turn.developments().use(Development.KNIGHT);
System.out.println(turn.color() + " plays knight");
        moveRobber(hex, whoToRob);
        turn.increaseArmyStrength();
    }


    int dfs(Player player, Xing i, Set<Path> visited, Path with) {
        int ans = 0;
        for (Path p : Board.adjacentPaths(i)) {
            if (visited.contains(p))
                continue;
            if (board.roadAt(p) != player && p != with)
                continue;
            visited.add(p);
            Xing[] ends = Board.endpoints(p);
            Xing otherEnd = ends[ends[0] == i ? 1 : 0];
            ans = Math.max(ans, 1 + dfs(player, otherEnd, visited, with));
            visited.remove(p);
        }
        return ans;
    }

    int roadLength(Player player) {
        return roadLengthWith(player, null);
    }

    int roadLengthWith(Player player, Path p) {
        int ans = 0;
        Set<Path> visited = new HashSet<Path>();
        for (Xing start : Board.allXings())
            ans = Math.max(ans, dfs(player, start, visited, p));
        return ans;
    }

    void addPlayer(Player player) {
        players.add(player);
    }

    void play() {
        init();
        placeFirstSettlements();

        turnNumber = 0;
        turn = players.get(players.size() - 1);

        while (true) {
System.out.println();
            turnNumber++;
            turn = players.get((index(turn) + 1) % n);
            diceRolled = 0;
            robberMoved = false;

            turn.bot().makeTurn();

            turn.developments().reenable();
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
            if (vp > 0)
                System.out.print(" (" + vp + " VP)");
            if (largestArmy == player && player.armyStrength() >= 3)
                System.out.print(" (2 ARMY)");
            if (longestRoad == player && roadLength(player) >= 5)
                System.out.print(" (2 ROAD)");
            System.out.println();
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
                Pair<Xing, Path> p = player.bot().placeFirstSettlements(it == 0);
                if (!Board.areAdjacent(p.first(), p.second()))
                    throw new RuntimeException("Cannot build a road not connected to a town");
                if (!board.canBuildTownAt(p.first(), false, player))
                    throw new RuntimeException("Cannot build a town here");
                board.buildTown(p.first(), new Town(player, false));
                board.buildRoad(p.second(), player);
                if (it == 1) {
                    for (Hex c : Board.adjacentHexes(p.first())) {
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
        for (Pair<Xing, Town> pair : board.allTowns())
            if (pair.second().player() == player)
                points += pair.second().isCity() ? 2 : 1;
        if (longestRoad == player && roadLength(player) >= 5)
            points += 2;
        if (largestArmy == player && player.armyStrength() >= 3)
            points += 2;
        points += player.developments().victoryPoint();
        return points;
    }

    boolean playerHasWon() {
        return points(turn) >= 10;
    }

    void updateLongestRoad() {
        int z = roadLength(turn);
        for (Player p : players)
            if (p != turn && roadLength(p) >= z)
                return;
        longestRoad = turn;
    }

    void updateLargestArmy() {
        int z = turn.armyStrength();
        for (Player p : players)
            if (p != turn && p.armyStrength() >= z)
                return;
        largestArmy = turn;
    }

    int index(Player player) {
        int ans = players.indexOf(player);
        if (ans < 0)
            throw new IllegalStateException("Internal: player not found");
        return ans;
    }

    Player player(Bot bot) {
        for (Player player : players)
            if (player.bot() == bot)
                return player;
        return null;
    }

}

