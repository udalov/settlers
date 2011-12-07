package settlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import settlers.util.Pair;

public class History {
    
    private final List<Pair<Player, List<Event>>> events = new ArrayList<Pair<Player, List<Event>>>();

    History() {
        nextTurn(null);
    }

    void nextTurn(Player player) {
        events.add(new Pair<Player, List<Event>>(player, new ArrayList<Event>()));
    }

    private void addEvent(Event e) {
        events.get(events.size() - 1).second().add(e);
    }

    public List<Pair<Player, List<Event>>> getAll() {
        List<Pair<Player, List<Event>>> ans = new ArrayList<Pair<Player, List<Event>>>(events.size());
        for (Pair<Player, List<Event>> pair : events)
            ans.add(new Pair<Player, List<Event>>(pair.first(), new ArrayList<Event>(pair.second())));
        return ans;
    }

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

    void initialRoad(Player pl, Path p) { addEvent(new Event(EventType.INITIAL_ROAD, pl, p)); }
    void initialSettlement(Player pl, Xing x) { addEvent(new Event(EventType.INITIAL_SETTLEMENT, pl, x)); }
    void rollDice(int n) { addEvent(new Event(EventType.ROLL_DICE, n)); }
    void robber(Hex h, Player robbed) { addEvent(new Event(EventType.ROBBER, h, robbed)); }
    void resources(Player pl, List<Resource> resources) { addEvent(new Event(EventType.RESOURCES, pl, resources)); }
    void discard(Player pl, List<Resource> resources) { addEvent(new Event(EventType.DISCARD, pl, resources)); }
    void road(Path p) { addEvent(new Event(EventType.ROAD, p)); }
    void settlement(Xing x) { addEvent(new Event(EventType.SETTLEMENT, x)); }
    void city(Xing x) { addEvent(new Event(EventType.CITY, x)); }
    void development() { addEvent(new Event(EventType.DEVELOPMENT)); }
    void knight() { addEvent(new Event(EventType.KNIGHT)); }
    void invention(Resource r1, Resource r2) { addEvent(new Event(EventType.INVENTION, r1, r2)); }
    void monopoly(Resource r, int got) { addEvent(new Event(EventType.MONOPOLY, r, got)); }
    void roadBuilding(Path p1, Path p2) { addEvent(new Event(EventType.ROAD_BUILDING, p1, p2)); }
    void longestRoad(int length) { addEvent(new Event(EventType.LONGEST_ROAD, length)); }
    void largestArmy(int size) { addEvent(new Event(EventType.LARGEST_ARMY, size)); }
    void change(String sell, String buy) { addEvent(new Event(EventType.CHANGE, sell, buy)); }
    void trade(Player pl, String sell, String buy) { addEvent(new Event(EventType.TRADE, pl, sell, buy)); }
    void victory(int victoryPoints) { addEvent(new Event(EventType.VICTORY, victoryPoints)); }

}

