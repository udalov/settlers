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

    public static Resource fromChar(char c) {
        for (Resource r : all())
            if (r.chr() == c)
                return r;
        return null;
    }

    public char chr() {
        return toString().charAt(0);
    }
}

