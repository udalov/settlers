package settlers;

public class Hex {

    private final int x;
    private final int y;

    Hex(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() { return x; }
    public int y() { return y; }
    public String toString() { return "("+x+","+y+")"; }
}

