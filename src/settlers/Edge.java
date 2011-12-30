package settlers;

public class Edge {

    private final Hex hex;
    private final int direction;

    Edge(Hex hex, int direction) {
        this.hex = hex;
        this.direction = direction;
    }

    public Hex hex() { return hex; }
    public int direction() { return direction; }
    public int x() { return hex.x(); }
    public int y() { return hex.y(); }
    public String toString() { return hex.x() + " " + hex.y() + " " + direction; }

    public boolean equals(Object o) {
        if (!(o instanceof Edge))
            return false;
        Edge p = (Edge)o;
        return hex.equals(p.hex) && direction == p.direction;
    }

    public int hashCode() {
        return (hex.hashCode() << 3) + direction;
    }
}

