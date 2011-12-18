package settlers;

import java.util.List;

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
            case RESOURCES:
            case DISCARD:
            case TRADE:
                return (Player)args[0];
            case ROBBER:
                return (Player)args[1];
            default:
                throw noRecord("player");
        }
    }

    public Path path() {
        switch (type) {
            case ROAD:
            case ROAD_BUILDING:
                return (Path)args[0];
            case INITIAL_ROAD:
                return (Path)args[1];
            default:
                throw noRecord("path");
        }
    }

    public Xing xing() {
        switch (type) {
            case SETTLEMENT:
            case CITY:
                return (Xing)args[0];
            case INITIAL_SETTLEMENT:
                return (Xing)args[1];
            default:
                throw noRecord("xing");
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
            case RESOURCES:
            case DISCARD:
                return (List<Resource>)args[1];
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

    public Path path2() {
        switch (type) {
            case ROAD_BUILDING:
                return (Path)args[1];
            default:
                throw noRecord("path2");
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
