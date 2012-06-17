package settlers;

public enum Development {
    KNIGHT,
    ROAD_BUILDING,
    INVENTION,
    MONOPOLY,
    VICTORY_POINT;

    public char chr() {
        return toString().charAt(0);
    }
}

