package settlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import settlers.util.Pair;

public class History {
    
    private final List<Pair<Player, List<Event>>> events = new ArrayList<Pair<Player, List<Event>>>();
    private final Game game;

    History(Game game) {
        this.game = game;
        nextTurn(null);
    }

    void nextTurn(Player player) {
        events.add(new Pair<Player, List<Event>>(player, new ArrayList<Event>()));
        game.eventHappened();
    }

    private void addEvent(Event e) {
        events.get(events.size() - 1).second().add(e);
        game.eventHappened();
    }

    private void addEvent(EventType type, Object... o) {
        addEvent(new Event(type, o));
    }

    public Pair<Player, Event> getLastEvent() {
        if (events.isEmpty())
            return null;
        Pair<Player, List<Event>> pair = events.get(events.size() - 1);
        if (pair.second().isEmpty())
            return null;
        return Pair.make(pair.first(), pair.second().get(pair.second().size() - 1));
    }

    public List<Pair<Player, List<Event>>> getAll() {
        List<Pair<Player, List<Event>>> ans = new ArrayList<Pair<Player, List<Event>>>(events.size());
        for (Pair<Player, List<Event>> pair : events)
            ans.add(new Pair<Player, List<Event>>(pair.first(), new ArrayList<Event>(pair.second())));
        return ans;
    }

    // TODO: test
    public List<Pair<Player, List<Event>>> getAllSinceMyLastTurn() {
        List<Pair<Player, List<Event>>> ans = new ArrayList<Pair<Player, List<Event>>>(events.size());
        if (events.isEmpty())
            return ans;
        final Player me = events.get(events.size() - 1).first();
        for (int i = events.size() - 2; i >= 0 && events.get(i).first() != me; i--)
            ans.add(new Pair<Player, List<Event>>(events.get(i).first(),
                new ArrayList<Event>(events.get(i).second())));
        Collections.reverse(ans);
        return ans;
    }

    public int size() {
        return events.size();
    }

    void initialRoad(Player pl, Path p) { addEvent(EventType.INITIAL_ROAD, pl, p); }
    void initialSettlement(Player pl, Xing x) { addEvent(EventType.INITIAL_SETTLEMENT, pl, x); }
    void rollDice(int n) { addEvent(EventType.ROLL_DICE, n); }
    void robber(Hex h, Player robbed) { addEvent(EventType.ROBBER, h, robbed); }
    void resources(Player pl, List<Resource> resources) { addEvent(EventType.RESOURCES, pl, resources); }
    void discard(Player pl, List<Resource> resources) { addEvent(EventType.DISCARD, pl, resources); }
    void road(Path p) { addEvent(EventType.ROAD, p); }
    void settlement(Xing x) { addEvent(EventType.SETTLEMENT, x); }
    void city(Xing x) { addEvent(EventType.CITY, x); }
    void development() { addEvent(EventType.DEVELOPMENT); }
    void knight() { addEvent(EventType.KNIGHT); }
    void invention(Resource r1, Resource r2) { addEvent(EventType.INVENTION, r1, r2); }
    void monopoly(Resource r, int got) { addEvent(EventType.MONOPOLY, r, got); }
    void roadBuilding(Path p1, Path p2) { addEvent(EventType.ROAD_BUILDING, p1, p2); }
    void longestRoad(int length) { addEvent(EventType.LONGEST_ROAD, length); }
    void largestArmy(int size) { addEvent(EventType.LARGEST_ARMY, size); }
    void change(String sell, String buy) { addEvent(EventType.CHANGE, sell, buy); }
    void trade(Player pl, String sell, String buy) { addEvent(EventType.TRADE, pl, sell, buy); }
    void victory(int victoryPoints) { addEvent(EventType.VICTORY, victoryPoints); }
    void exception(Exception exception) { addEvent(EventType.EXCEPTION, exception); }

}

