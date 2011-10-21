package settlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardStack {

    private final Map<Resource, Integer> resources =
        new HashMap<Resource, Integer>();

    CardStack() {
        for (Resource r : Resource.class.getEnumConstants())
            resources.put(r, 0);
    }

    public int howMany(Resource r) { return resources.get(r); }

    public int size() {
        int ans = 0;
        for (Resource r : Resource.class.getEnumConstants())
            ans += resources.get(r);
        return ans;
    }

    public List<Resource> list() {
        List<Resource> list = new ArrayList<Resource>();
        for (Resource r : Resource.class.getEnumConstants())
            for (int i = 0, n = resources.get(r); i < n; i++)
                list.add(r);
        return list;
    }

    public boolean areThere(String needed) {
        for (Resource r : Resource.class.getEnumConstants()) {
            int x = needed.replaceAll("[^" + r.toString().charAt(0) + "]", "").length();
            if (resources.get(r) < x)
                return false;
        }
        return true;
    }

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
            throw new RuntimeException("Not enough resources: " +
                (x - howMany(r)) + " more " + r + " needed!");
        resources.put(r, resources.get(r) - x);
    }

    public String toString() {
        String ans = "";
        for (Resource r : list())
            ans += r.toString().charAt(0);
        return ans;
    }

}

