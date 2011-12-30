package settlers;

public class Node {

    private final Hex hex;
    private final int direction;

    Node(Hex hex, int direction) {
        this.hex = hex;
        this.direction = direction;
    }

    public Hex hex() { return hex; }
    public int direction() { return direction; }
    public int x() { return hex.x(); }
    public int y() { return hex.y(); }
    public String toString() { return hex.x() + " " + hex.y() + " " + direction; }

    public boolean equals(Object o) {
        if (!(o instanceof Node))
            return false;
        Node x = (Node)o;
        return hex.equals(x.hex) && direction == x.direction;
    }

    public int hashCode() {
        return (hex.hashCode() << 3) + direction;
    }
}

