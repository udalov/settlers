package settlers;

public enum Resource {
    BRICK,
    WOOL,
    ORE,
    GRAIN,
    LUMBER;

    public static Resource fromChar(char c) {
        for (Resource r : values())
            if (r.chr() == c)
                return r;
        return null;
    }

    public char chr() {
        return toString().charAt(0);
    }
}

