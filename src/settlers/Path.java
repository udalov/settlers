package settlers;

public class Path {

    private final Hex hex;
    private final int direction;

    Path(Hex hex, int direction) {
        this.hex = hex;
        this.direction = direction;
    }

    public Hex hex() { return hex; }
    public int direction() { return direction; }
    public int x() { return hex.x(); }
    public int y() { return hex.y(); }
    public String toString() { return "["+hex.x()+","+hex.y()+","+direction+"]"; }
}

