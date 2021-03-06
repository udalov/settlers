package settlers;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import settlers.util.Util;

public class ResourceDeck {

    private final Map<Resource, Integer> resources =
        new EnumMap<Resource, Integer>(Resource.class);

    ResourceDeck() {
        for (Resource r : Resource.values())
            resources.put(r, 0);
    }

    public int howMany(Resource r) { return r == null ? 0 : resources.get(r); }

    public int size() {
        int ans = 0;
        for (Resource r : Resource.values())
            ans += resources.get(r);
        return ans;
    }

    public List<Resource> list() {
        List<Resource> list = new ArrayList<Resource>();
        for (Resource r : Resource.values())
            for (int i = 0, n = resources.get(r); i < n; i++)
                list.add(r);
        return list;
    }

    public boolean areThere(String needed) {
        if (needed == null || "".equals(needed))
            return true;
        for (Resource r : Resource.values()) {
            int x = Util.numberOfOccurrences(r.chr(), needed);
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
            throw new GameException("Not enough resources: " +
                (x - howMany(r)) + " more " + r + " needed!");
        resources.put(r, resources.get(r) - x);
    }

    void add(String res) {
        for (Resource r : Resource.values()) {
            int x = Util.numberOfOccurrences(r.chr(), res);
            if (x > 0)
                add(r, x);
        }
    }

    void sub(String res) {
        for (Resource r : Resource.values()) {
            int x = Util.numberOfOccurrences(r.chr(), res);
            if (x > 0)
                sub(r, x);
        }
    }

    public String toString() {
        return Util.toResourceString(list());
    }
}

