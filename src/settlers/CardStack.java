package settlers;

import java.util.HashMap;
import java.util.Map;

public class CardStack {

    private final Map<Resource, Integer> resources = new HashMap<Resource, Integer>();

    CardStack() {
        for (Resource r : Resource.class.getEnumConstants()) {
            resources.put(r, 0);
        }
    }

    public int howMany(Resource r) { return resources.get(r); }

    public int brick() { return howMany(Resource.BRICK); }
    public int wool() { return howMany(Resource.WOOL); }
    public int ore() { return howMany(Resource.ORE); }
    public int grain() { return howMany(Resource.GRAIN); }
    public int lumber() { return howMany(Resource.LUMBER); }

    void add(Resource r, int x) {
        resources.put(r, resources.get(r) + x);
    }

    void sub(Resource r, int x) {
        if (howMany(r) < x)
            throw new RuntimeException("Not enough resources: " + (x - howMany(r)) + " more " + r + " needed!");
        resources.put(r, resources.get(r) - x);
    }

}

