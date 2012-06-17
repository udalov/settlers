package settlers;

public class Edge {

    public final Hex hex;
    public final int direction;

    Edge(Hex hex, int direction) {
        this.hex = hex;
        this.direction = direction;
    }

    public String toString() { return hex.x + " " + hex.y + " " + direction; }

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

