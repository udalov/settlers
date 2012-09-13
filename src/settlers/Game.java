package settlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
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

    private abstract class CommonAPI {
        protected final Game game;

        CommonAPI() { game = Game.this; }

        public List<Player> players() { return game.players(); }
        public Board board() { return game.board; }
        public History history() { return game.history; }
        public Player largestArmy() { return game.largestArmy; }
        public Player longestRoad() { return game.longestRoad; }
        public int roadLength(Player p) { return game.roadLength(p); }
        public int armyStrength(Player p) { return game.armyStrength(p); }
        public int points(Player p) { return game.points(p); }
        public Player turn() { return turn; }
        public Hex robber() { return robber; }
        public Player roadAt(Edge p) { return game.roadAt(p); }
        public Town townAt(Node i) { return game.townAt(i); }
    }

    @SuppressWarnings("UnusedDeclaration")
    public class API extends CommonAPI {
        private Bot bot;

        void setBot(Bot bot) { this.bot = bot; }

        private void checkTurn() {
            if (turn != me())
                throw new GameException("You cannot do anything not on your turn");
        }
        private void checkRobber() {
            if (diceRolled == 7 && robberMoveStatus == RobberMoveStatus.NEEDS_TO_MOVE)
                throw new GameException("You must move the robber right after rolling 7");
        }
        private void check() {
            checkTurn();
            checkRobber();
        }

        public Player me() { return player(bot); }
        public Random rnd() { return game.rnd; }
        public int turnNumber() { return turnNumber; }

        public ResourceDeck cards() { return me().cards(); }
        public DevelopmentDeck developments() { return me().developments(); }
        public ResourceDeck bank() { return game.bank; }

        public int rollDice() { check(); return game.rollDice(); }

        public List<Player> robbable(Hex c)
            { return game.robbable(c); }
        public void moveRobber(Hex c, Player whoToRob) {
            checkTurn();
            if (robberMoveStatus == RobberMoveStatus.NON_MOVABLE)
                throw new GameException("You cannot move the robber after not rolling 7");
            if (robberMoveStatus == RobberMoveStatus.MOVED)
                throw new GameException("You cannot move the robber twice");
            game.moveRobber(c, whoToRob);
        }

        public boolean canBuildSettlementAt(Node i, boolean mustBeRoad)
            { return game.canBuildSettlementAt(i, mustBeRoad, me()); }
        public boolean canBuildRoadAt(Edge p)
            { return game.canBuildRoadAt(p, me()); }

        public int settlementsLeft()
            { return game.settlementsLeft(me()); }
        public int citiesLeft()
            { return game.citiesLeft(me()); }
        public int roadsLeft()
            { return game.roadsLeft(me()); }

        public void buildSettlement(Node i)
            { check(); game.buildSettlement(i); }
        public void buildCity(Node i)
            { check(); game.buildCity(i); }
        public void buildRoad(Edge p)
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
        public void roadBuilding(Edge p1, Edge p2)
            { check(); game.roadBuilding(p1, p2); }
        public void invention(Resource r1, Resource r2)
            { check(); game.invention(r1, r2); }
        public void knight(Hex hex, Player whoToRob)
            { check(); game.knight(hex, whoToRob); }

        public int roadLengthWith(Player player, Edge p)
            { return game.roadLengthWith(player, p); }

        public List<TradeResult> trade(String sell, String buy)
            { check(); return game.trade(new TradeOffer(game, me(), sell, buy)); }
        public TradeResult acceptOffer(TradeOffer offer)
            { return offer.accept(me()); }
        public TradeResult declineOffer(TradeOffer offer)
            { return offer.decline(me()); }
        public TradeResult counterOffer(TradeOffer offer, String sell, String buy)
            { return offer.counteroffer(new TradeOffer(game, me(), sell, buy)); }
    }

    public final class RunAPI extends CommonAPI {
        RunAPI(boolean visual) {
            game.visual = visual;
        }
        public void play() { game.play(); }
        public void next() {
            synchronized(game) {
                game.notifyAll();
            }
        }
        public boolean isFinished() { return finished; }
        public void addPlayer(Player p) { game.addPlayer(p); }
        public ResourceDeck cards(Player p) { return p.cards(); }
        public DevelopmentDeck developments(Player p) { return p.developments(); }
        public void beforeRepaint() {
            // TODO: invent something different
            try { Thread.sleep(50); } catch (InterruptedException ignored) { }
            synchronized(game) { }
        }
    }

    private static final int MAX_SETTLEMENTS = 5;
    private static final int MAX_CITIES = 4;
    private static final int MAX_ROADS = 15;
    private static final int EACH_RESOURCE = 19;
    private static final int KNIGHTS = 14;
    private static final int ROAD_BUILDINGS = 2;
    private static final int INVENTIONS = 2;
    private static final int MONOPOLIES = 2;
    private static final int VICTORY_POINTS = 5;

    private static final int POINTS_TO_WIN = 10;
    public static final int MINIMUM_ROAD_LENGTH = 5;
    public static final int MINIMUM_ARMY_STRENGTH = 3;

    private final Random rnd;

    private final List<Player> players = new ArrayList<Player>();
    private final Board board;
    private int n;

    private int turnNumber;
    private Player turn;
    private int diceRolled;
    private RobberMoveStatus robberMoveStatus;
    private final ResourceDeck bank = new ResourceDeck();
    private Player largestArmy;
    private Player longestRoad;
    private Hex robber;
    private final List<Development> developments = new ArrayList<Development>();
    private final Map<Edge, Player> roads = new HashMap<Edge, Player>();
    private final Map<Node, Town> towns = new HashMap<Node, Town>();
    private final Map<Player, Integer> armyStrength = new HashMap<Player, Integer>();

    private final History history = new History(this);

    private boolean visual = false;
    private boolean finished = false;

    private enum RobberMoveStatus {
        NON_MOVABLE,
        NEEDS_TO_MOVE,
        MOVED
    }

    Game(long randSeed) {
        rnd = randSeed == 0 ? new Random() : new Random(randSeed);
        board = Board.create(rnd);
        for (Hex hex : Board.allHexes())
            if (board.numberAt(hex) == 0)
                robber = hex;
        for (int i = 0; i < KNIGHTS; i++)
            developments.add(Development.KNIGHT);
        for (int i = 0; i < ROAD_BUILDINGS; i++)
            developments.add(Development.ROAD_BUILDING);
        for (int i = 0; i < INVENTIONS; i++)
            developments.add(Development.INVENTION);
        for (int i = 0; i < MONOPOLIES; i++)
            developments.add(Development.MONOPOLY);
        for (int i = 0; i < VICTORY_POINTS; i++)
            developments.add(Development.VICTORY_POINT);
        Collections.shuffle(developments, rnd);
        for (Resource r : Resource.values())
            bank.add(r, EACH_RESOURCE);
    }

    private List<Player> players() { return Collections.unmodifiableList(players); }
    private Player roadAt(Edge p) { return roads.get(p); }
    private Town townAt(Node i) { return towns.get(i); }

    // TODO: kill dependency
    History history() { return history; }





    void eventHappened() {
        if (!visual)
            return;
        try {
            synchronized(this) {
                wait();
            }
        } catch (InterruptedException ie) {
            // do nothing
        }
    }

    private List<TradeResult> trade(TradeOffer offer) {
        List<TradeResult> ans = new ArrayList<TradeResult>();
        for (Player p : players)
            if (p != offer.trader() && p.cards().areThere(offer.buy()))
                ans.add(p.bot().trade(offer));
        return ans;
    }

    private int rollDice() {
        if (diceRolled != 0)
            throw new GameException("You cannot roll the dice twice a turn");
        diceRolled = rnd.nextInt(6) + rnd.nextInt(6) + 2;
        if (diceRolled == 7) {
            robberMoveStatus = RobberMoveStatus.NEEDS_TO_MOVE;
            history.rollDice(diceRolled, Collections.<Player, List<Resource>>emptyMap());
            for (Player p : players) {
                int were = p.cards().size();
                if (were > 7) {
                    List<Resource> discard = p.bot().discardHalfOfTheCards();
                    if (discard == null)
                        throw new GameException("You cannot discard null");
                    for (Resource r : discard) {
                        if (r == null)
                            throw new GameException("You cannot discard null");
                        p.cards().sub(r, 1);
                        bank.add(r, 1);
                    }
                    if (p.cards().size() != (were + 1) / 2)
                        throw new GameException("You must discard half of your cards");
                    history.discard(p, discard);
                }
            }
        } else {
            int[] neededResCards = new int[Resource.values().length];
            Map<Player, List<Resource>> income = new HashMap<Player, List<Resource>>();
            for (Player p : players)
                income.put(p, new ArrayList<Resource>());
            for (int step = 0; step < 2; step++) {
                for (Hex hex : Board.allHexes()) {
                    if (board.numberAt(hex) != diceRolled)
                        continue;
                    if (robber == hex)
                        continue;
                    for (Node x : Board.adjacentNodes(hex)) {
                        Town town = townAt(x);
                        if (town == null)
                            continue;
                        Resource res = board.resourceAt(hex);
                        int q = town.isCity() ? 2 : 1;
                        int index = res.ordinal();
                        if (step == 0) {
                            neededResCards[index] += q;
                        } else if (neededResCards[index] <= bank.howMany(res)) {
                            town.player().cards().add(res, q);
                            income.get(town.player()).add(res);
                            if (q == 2)
                                income.get(town.player()).add(res);
                            bank.sub(res, q);
                        }
                    }
                }
            }
            for (Player p : players)
                income.put(p, Util.sort(income.get(p)));
            history.rollDice(diceRolled, income);
        }
        return diceRolled;
    }

    private List<Player> robbable(Hex hex) {
        Set<Player> ans = new HashSet<Player>();
        for (Node x : Board.adjacentNodes(hex)) {
            if (towns.get(x) == null)
                continue;
            Player p = towns.get(x).player();
            if (p != turn && p.cardsNumber() > 0)
                ans.add(p);
        }
        return new ArrayList<Player>(ans);
    }

    private void moveRobber(Hex hex, Player whoToRob) {
        if (hex == null)
            throw new GameException("You cannot move the robber to null");
        if (hex == robber)
            throw new GameException("You cannot leave the robber at his current position");
        if (whoToRob == turn)
            throw new GameException("You cannot rob yourself");
        if (whoToRob != null && whoToRob.cardsNumber() == 0)
            throw new GameException("You cannot rob a player who has no cards");
        List<Player> robbable = robbable(hex);
        if (!robbable.isEmpty() && whoToRob == null)
            throw new GameException("You must rob somebody");
        if ((robbable.isEmpty() && whoToRob != null) ||
            (!robbable.isEmpty() && !robbable.contains(whoToRob)))
            throw new GameException("You cannot rob a player not having a town near the robber");
        robber = hex;
        robberMoveStatus = RobberMoveStatus.MOVED;
        if (whoToRob == null) {
            history.robber(hex, whoToRob);
            return;
        }
        List<Resource> list = whoToRob.cards().list();
        Resource r = list.get(rnd.nextInt(list.size()));
        whoToRob.cards().sub(r, 1);
        turn.cards().add(r, 1);
        history.robber(hex, whoToRob);
    }

    private int settlementsLeft(Player p) {
        int placed = 0;
        for (Town t : towns.values())
            if (t.player() == p && !t.isCity())
                placed++;
        return MAX_SETTLEMENTS - placed;
    }

    private int citiesLeft(Player p) {
        int placed = 0;
        for (Town t : towns.values())
            if (t.player() == p && t.isCity())
                placed++;
        return MAX_CITIES - placed;
    }

    private int roadsLeft(Player p) {
        int placed = 0;
        for (Player q : roads.values())
            if (p == q)
                placed++;
        return MAX_ROADS - placed;
    }

    private void buildSettlement(Node x) {
        if (x == null)
            throw new GameException("You cannot build a settlement at null");
        if (settlementsLeft(turn) == 0)
            throw new GameException("You do not have any settlements left");
        if (!turn.cards().areThere("BWGL"))
            throw new GameException("Not enough resources to build a settlement");
        if (!canBuildSettlementAt(x, true, turn))
            throw new GameException("You cannot build a settlement here");
        towns.put(x, new Town(turn, false));
        turn.cards().sub("BWGL");
        bank.add("BWGL");
        history.settlement(x);
    }

    private void buildCity(Node x) {
        if (x == null)
            throw new GameException("You cannot build a town at null");
        if (citiesLeft(turn) == 0)
            throw new GameException("You do not have any cities left");
        if (!turn.cards().areThere("OOOGG"))
            throw new GameException("Not enough resources to build a city");
        if (townAt(x) == null)
            throw new GameException("You must first build a settlement to be able to upgrade it");
        if (townAt(x).isCity())
            throw new GameException("You cannot build a city over an existing city");
        if (townAt(x).player() != turn)
            throw new GameException("You cannot upgrade other player's settlement");
        towns.put(x, new Town(turn, true));
        turn.cards().sub("OOOGG");
        bank.add("OOOGG");
        history.city(x);
    }

    private void buildRoad(Edge p) {
        if (p == null)
            throw new GameException("You cannot build a road at null");
        if (roadsLeft(turn) == 0)
            throw new GameException("You do not have any roads left");
        if (!turn.cards().areThere("BL"))
            throw new GameException("Not enough resources to build a road");
        if (!canBuildRoadAt(p, turn))
            throw new GameException("You cannot build a road here");
        roads.put(p, turn);
        turn.cards().sub("BL");
        bank.add("BL");
        updateLongestRoad();
        history.road(p);
    }

    private boolean hasPort(Resource r, Player player) {
        for (Node x : towns.keySet()) {
            if (towns.get(x).player() == player) {
                Harbor harbor = board.harborAt(x);
                if (harbor != null && harbor.resource() == r)
                    return true;
            }
        }
        return false;
    }

    private boolean hasPort3to1(Player player) {
        return hasPort(null, player);
    }

    private boolean canChange(String sell, String buy, Player player) {
        if (sell == null || buy == null || "".equals(sell) || "".equals(buy))
            return false;
        if (!Util.isResourceString(sell) || !Util.isResourceString(buy))
            throw new GameException("Invalid characters in change string");
        if (!bank.areThere(buy) || !player.cards().areThere(sell))
            return false;
        int res = 0;
        for (Resource r : Resource.values()) {
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

    private void change(String sell, String buy) {
        if (!canChange(sell, buy, turn))
            throw new GameException("You cannot change " + sell + " to " + buy);
        turn.cards().sub(sell);
        turn.cards().add(buy);
        bank.add(sell);
        bank.sub(buy);
        history.change(sell, buy);
    }

    private boolean getIfPossible(String what) {
        if (what == null || "".equals(what))
            return true;
        if (!Util.isResourceString(what))
            throw new GameException("Invalid characters in resource string");
        Map<Resource, Integer> left = new EnumMap<Resource, Integer>(Resource.class);
        String buy = "";
        for (Resource r : Resource.values()) {
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
        boolean has3to1 = hasPort3to1(turn);
        String sell = "";
        int buyingIndex = 0;
        // first 2:1, then 3:1 or 4:1
        it: for (int it = 0; it < 2; it++) {
            for (Resource r : Resource.values()) {
                if (it == 0 && !hasPort(r, turn))
                    continue;
                int x = left.get(r);
                if (x == 0)
                    continue;
                int coeff = it == 0 ? 2 : has3to1 ? 3 : 4;
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
        // there may be not enough resources in bank
        if (!canChange(sell, buy, turn))
            return false;
        change(sell, buy);
        return true;
    }

    private void drawDevelopment() {
        if (developments.isEmpty())
            throw new GameException("No more developments left in the game");
        if (!turn.cards().areThere("WOG"))
            throw new GameException("Not enough resources to draw a development");
        Development d = developments.remove(developments.size() - 1);
        turn.developments().add(d);
        turn.cards().sub("WOG");
        bank.add("WOG");
        history.development();
    }

    private void monopoly(Resource r) {
        if (r == null)
            throw new GameException("You cannot declare monopoly on null");
        turn.developments().use(Development.MONOPOLY);
        int got = 0;
        for (Player p : players) {
            if (p == turn)
                continue;
            int x = p.cards().howMany(r);
            got += x;
            turn.cards().add(r, x);
            p.cards().sub(r, x);
        }
        history.monopoly(r, got);
    }

    private void roadBuilding(Edge p1, Edge p2) {
        turn.developments().use(Development.ROAD_BUILDING);
        if (roadsLeft(turn) == 0)
            throw new GameException("You do not have any roads left to use road building card");
        if (roadsLeft(turn) == 1) {
            if (p1 == null) { Edge p = p1; p1 = p2; p2 = p; }
            if (p2 != null)
                throw new GameException("You have only 1 road left to use road building card");
        }
        if (!canBuildRoadAt(p1, turn))
            throw new GameException("You cannot build a road here");
        roads.put(p1, turn);
        if (p2 != null) {
            if (!canBuildRoadAt(p2, turn))
                throw new GameException("You cannot build a road here");
            roads.put(p2, turn);
        }
        updateLongestRoad();
        history.roadBuilding(p1, p2);
    }

    private void invention(Resource r1, Resource r2) {
        if (r1 == null || r2 == null)
            throw new GameException("You cannot use invention card on null");
        if (bank.howMany(r1) == 0 || bank.howMany(r2) == 0)
            throw new GameException("You cannot use invention card on non-existing resources");
        turn.developments().use(Development.INVENTION);
        turn.cards().add(r1, 1);
        turn.cards().add(r2, 1);
        bank.sub(r1, 1);
        bank.sub(r2, 1);
        history.invention(r1, r2);
    }

    private void knight(Hex hex, Player whoToRob) {
        turn.developments().use(Development.KNIGHT);
        Integer army = armyStrength.get(turn);
        armyStrength.put(turn, army != null ? army + 1 : 1);
        history.knight();
        updateLargestArmy();
        moveRobber(hex, whoToRob);
    }


    private boolean canBuildSettlementAt(Node i, boolean mustBeRoad, Player player) {
        if (i == null || towns.get(i) != null)
            return false;
        for (Node j : Board.adjacentNodes(i))
            if (towns.get(j) != null)
                return false;
        if (!mustBeRoad)
            return true;
        for (Edge p : Board.adjacentEdges(i))
            if (roads.get(p) == player)
                return true;
        return false;
    }

    private boolean canBuildRoadAt(Edge p, Player player) {
        if (p == null || roads.get(p) != null)
            return false;
        for (Node i : Board.endpoints(p)) {
            Town t = towns.get(i);
            if (t != null && t.player() != player)
                continue;
            for (Edge q : Board.adjacentEdges(i)) {
                if (q == p)
                    continue;
                if (roads.get(q) == player)
                    return true;
            }
        }
        return false;
    }



    private int dfsRoadLength(Player player, Node i, Set<Edge> visited, Edge with) {
        int ans = 0;
        for (Edge p : Board.adjacentEdges(i)) {
            if (visited.contains(p))
                continue;
            if (roadAt(p) != player && p != with)
                continue;
            visited.add(p);
            Node[] ends = Board.endpoints(p);
            Node otherEnd = ends[ends[0] == i ? 1 : 0];
            ans = Math.max(ans, 1 + dfsRoadLength(player, otherEnd, visited, with));
            visited.remove(p);
        }
        return ans;
    }

    private int roadLengthWith(Player player, Edge p) {
        if (player == null)
            return 0;
        int ans = 0;
        Set<Edge> visited = new HashSet<Edge>();
        for (Node start : Board.allNodes())
            ans = Math.max(ans, dfsRoadLength(player, start, visited, p));
        return ans;
    }

    private int roadLength(Player player) {
        return roadLengthWith(player, null);
    }

    private int armyStrength(Player player) {
        Integer ans = armyStrength.get(player);
        return ans == null ? 0 : ans;
    }

    private void addPlayer(Player player) {
        players.add(player);
    }

    private boolean nextTurn() {
        turn = players.get(turnNumber++ % n);
        diceRolled = 0;
        robberMoveStatus = RobberMoveStatus.NON_MOVABLE;
        history.nextTurn(turn);

        turn.bot().makeTurn();

        turn.developments().reenable();

        if (playerHasWon()) {
            finished = true;
            history.victory(turn.developments().victoryPoint());
            return true;
        }

        if (diceRolled == 0)
            throw new GameException("You must roll the dice once a turn");
        if (diceRolled == 7 && robberMoveStatus == RobberMoveStatus.NEEDS_TO_MOVE)
            throw new GameException("You must move the robber if you rolled 7");

        return false;
    }

    private void play() {
        n = players.size();
        Collections.shuffle(players, rnd);
        turnNumber = -1;
        eventHappened();

        try {
            placeInitialSettlements();
            while (!nextTurn());
        } catch (Exception e) {
            finished = true;
            history.exception(e);
        }
    }

    private void placeInitialSettlements() {
        for (int it = 0; it < 2; it++) {
            for (int i = it * (n - 1); 0 <= i && i < n; i += 1 - 2*it) {
                Player player = players.get(i);
                Pair<Node, Edge> p = player.bot().placeInitialSettlements(it == 0);
                if (p == null || p.first == null || p.second == null)
                    throw new GameException("You cannot build a first settlement at null");
                if (!Board.areAdjacent(p.first, p.second))
                    throw new GameException("You cannot build a road not connected to a town");
                if (!canBuildSettlementAt(p.first, false, player))
                    throw new GameException("You cannot build a town here");
                towns.put(p.first, new Town(player, false));
                history.initialSettlement(player, p.first);
                roads.put(p.second, player);
                history.initialRoad(player, p.second);
                if (it == 1) {
                    for (Hex c : Board.adjacentHexes(p.first)) {
                        Resource r = board.resourceAt(c);
                        if (r != null) {
                            player.cards().add(r, 1);
                            bank.sub(r, 1);
                        }
                    }
                }
            }
        }
        turnNumber = 0;
    }

    private int points(Player player, boolean includeVP) {
        int points = 0;
        for (Node i : towns.keySet())
            if (towns.get(i).player() == player)
                points += towns.get(i).isCity() ? 2 : 1;
        if (longestRoad == player && roadLength(player) >= MINIMUM_ROAD_LENGTH)
            points += 2;
        if (largestArmy == player && armyStrength(player) >= MINIMUM_ARMY_STRENGTH)
            points += 2;
        if (includeVP)
            points += player.developments().victoryPoint();
        return points;
    }

    private int points(Player player) {
        return points(player, false);
    }

    private boolean playerHasWon() {
        return points(turn, true) >= POINTS_TO_WIN;
    }

    private void updateLongestRoad() {
        int z = roadLength(turn);
        for (Player p : players)
            if (p != turn && roadLength(p) >= z)
                return;
        if (z >= MINIMUM_ROAD_LENGTH && longestRoad != turn)
            history.longestRoad(z);
        longestRoad = turn;
    }

    private void updateLargestArmy() {
        int z = armyStrength(turn);
        for (Player p : players)
            if (p != turn && armyStrength(p) >= z)
                return;
        if (z >= MINIMUM_ARMY_STRENGTH && largestArmy != turn)
            history.largestArmy(z);
        largestArmy = turn;
    }

    private Player player(Bot bot) {
        for (Player player : players)
            if (player.bot() == bot)
                return player;
        return null;
    }

}

