package settlers;

public class Hex {

    public final int x;
    public final int y;

    Hex(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() { return x + " " + y; }

    public boolean equals(Object o) {
        if (!(o instanceof Hex))
            return false;
        Hex h = (Hex)o;
        return x == h.x && y == h.y;
    }

    public int hashCode() {
        return (x << 3) + y;
    }
}

