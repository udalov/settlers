package settlers;

public class Node {

    public final Hex hex;
    public final int direction;

    Node(Hex hex, int direction) {
        this.hex = hex;
        this.direction = direction;
    }

    public String toString() { return hex.x + " " + hex.y + " " + direction; }

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

