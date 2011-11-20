package settlers;

import java.util.ArrayList;
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

    void initialRoad(Player pl, Path p) { addEvent(new Event(EventType.INITIAL_ROAD, pl, p)); }
    void initialSettlement(Player pl, Xing x) { addEvent(new Event(EventType.INITIAL_SETTLEMENT, pl, x)); }
    void rollDice(int n) { addEvent(new Event(EventType.ROLL_DICE, n)); }
    void resources(Player pl, Resource r, int n) { addEvent(new Event(EventType.RESOURCES, pl, r, n)); }
    void road(Path p) { addEvent(new Event(EventType.ROAD, p)); }
    void settlement(Xing x) { addEvent(new Event(EventType.SETTLEMENT, x)); }
    void city(Xing x) { addEvent(new Event(EventType.CITY, x)); }
    void development() { addEvent(new Event(EventType.DEVELOPMENT)); }
    void knight(Hex h, Player robbed) { addEvent(new Event(EventType.KNIGHT, h, robbed)); }
    void invention(Resource r1, Resource r2) { addEvent(new Event(EventType.INVENTION, r1, r2)); }
    void monopoly(Resource r) { addEvent(new Event(EventType.MONOPOLY, r)); }
    void roadBuilding(Path p1, Path p2) { addEvent(new Event(EventType.ROAD_BUILDING, p1, p2)); }
    void change(String sell, String buy) { addEvent(new Event(EventType.CHANGE, sell, buy)); }
    void trade() { } // TODO
    void victory() { addEvent(new Event(EventType.VICTORY)); }

}

