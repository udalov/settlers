package settlers;

public enum Development {
    KNIGHT,
    ROAD_BUILDING,
    INVENTION,
    MONOPOLY,
    VICTORY_POINT;

    public static Development[] all() {
        return Development.class.getEnumConstants();
    }

    public char chr() {
        return toString().charAt(0);
    }
}

