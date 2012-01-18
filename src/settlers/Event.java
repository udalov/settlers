package settlers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Event {
    
    private final EventType type;
    private final Object[] args;

    Event(EventType type, Object... args) {
        this.type = type;
        this.args = args;
    }

    public EventType type() { return type; }

    private RuntimeException noRecord(String field) {
        return new GameException("Event " + type + " has no " + field + " record");
    }

    public Player player() {
        switch (type) {
            case INITIAL_ROAD:
            case INITIAL_SETTLEMENT:
            case DISCARD:
            case TRADE:
                return (Player)args[0];
            case ROBBER:
                return (Player)args[1];
            default:
                throw noRecord("player");
        }
    }

    public Edge edge() {
        switch (type) {
            case ROAD:
            case ROAD_BUILDING:
                return (Edge)args[0];
            case INITIAL_ROAD:
                return (Edge)args[1];
            default:
                throw noRecord("edge");
        }
    }

    public Node node() {
        switch (type) {
            case SETTLEMENT:
            case CITY:
                return (Node)args[0];
            case INITIAL_SETTLEMENT:
                return (Node)args[1];
            default:
                throw noRecord("node");
        }
    }

    public int number() {
        switch (type) {
            case ROLL_DICE:
            case LONGEST_ROAD:
            case LARGEST_ARMY:
            case VICTORY:
                return (Integer)args[0];
            case MONOPOLY:
                return (Integer)args[1];
            default:
                throw noRecord("number");
        }
    }

    @SuppressWarnings("unchecked")
    public Map<Player, List<Resource>> income() {
        switch (type) {
            case ROLL_DICE:
                Map<Player, List<Resource>> map = (Map<Player, List<Resource>>)args[1];
                Map<Player, List<Resource>> ans = new HashMap<Player, List<Resource>>();
                for (Player p : map.keySet())
                    ans.put(p, Collections.unmodifiableList(map.get(p)));
                return ans;
            default:
                throw noRecord("income");
        }
    }

    public Hex hex() {
        switch (type) {
            case ROBBER:
                return (Hex)args[0];
            default:
                throw noRecord("hex");
        }
    }

    @SuppressWarnings("unchecked")
    public List<Resource> resources() {
        switch (type) {
            case DISCARD:
                return Collections.unmodifiableList((List<Resource>)args[1]);
            default:
                throw noRecord("resources");
        }
    }

    public Resource resource() {
        switch (type) {
            case INVENTION:
            case MONOPOLY:
                return (Resource)args[0];
            default:
                throw noRecord("resource");
        }
    }

    public Resource resource2() {
        switch (type) {
            case INVENTION:
                return (Resource)args[1];
            default:
                throw noRecord("resource2");
        }
    }

    public Edge edge2() {
        switch (type) {
            case ROAD_BUILDING:
                return (Edge)args[1];
            default:
                throw noRecord("edge2");
        }
    }

    public String sell() {
        switch (type) {
            case CHANGE:
                return (String)args[0];
            case TRADE:
                return (String)args[1];
            default:
                throw noRecord("sell");
        }
    }

    public String buy() {
        switch (type) {
            case CHANGE:
                return (String)args[1];
            case TRADE:
                return (String)args[2];
            default:
                throw noRecord("buy");
        }
    }

    public Exception exception() {
        switch (type) {
            case EXCEPTION:
                return (Exception)args[0];
            default:
                throw noRecord("exception");
        }
    }
}
