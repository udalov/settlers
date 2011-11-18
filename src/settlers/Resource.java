package settlers;

public enum Resource {
    BRICK,
    WOOL,
    ORE,
    GRAIN,
    LUMBER;

    public static Resource[] all() {
        return Resource.class.getEnumConstants();
    }

    public char chr() {
        return toString().charAt(0);
    }

    public int index() {
        // TODO: easier
        if (this == BRICK) return 0;
        if (this == WOOL) return 1;
        if (this == ORE) return 2;
        if (this == GRAIN) return 3;
        if (this == LUMBER) return 4;
        return -1;
    }
}

