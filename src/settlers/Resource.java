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
}

