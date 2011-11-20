package settlers;

public class Event {
    
    private final EventType type;
    private final Object[] args;

    Event(EventType type, Object... args) {
        this.type = type;
        this.args = args;
    }

    public EventType type() { return type; }
    public Object arg(int i) { return args[i]; }

}
