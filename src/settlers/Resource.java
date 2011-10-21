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
}

