package settlers;

import java.util.EnumMap;
import java.util.Map;

public class DevelopmentStack {

    private final Map<Development, Integer> developments =
        new EnumMap<Development, Integer>(Development.class);
    private final Map<Development, Integer> disabled =
        new EnumMap<Development, Integer>(Development.class);

    DevelopmentStack() {
        for (Development d : Development.all()) {
            developments.put(d, 0);
            disabled.put(d, 0);
        }
    }

    public int howMany(Development d) {
        return developments.get(d);
    }

    public int howManyDisabled(Development d) {
        return disabled.get(d);
    }

    public int size() {
        int ans = 0;
        for (Development d : Development.all()) {
            ans += developments.get(d);
            ans += disabled.get(d);
        }
        return ans;
    }

    public int knight() { return howMany(Development.KNIGHT); }
    public int roadBuilding() { return howMany(Development.ROAD_BUILDING); }
    public int invention() { return howMany(Development.INVENTION); }
    public int monopoly() { return howMany(Development.MONOPOLY); }
    public int victoryPoint() { return howMany(Development.VICTORY_POINT); }

    void add(Development d) {
        disabled.put(d, 1);
    }

    void use(Development d) {
        if (developments.get(d) == 0 && disabled.get(d) > 0)
            throw new RuntimeException("You cannot use " + d + " development at this moment!");
        if (developments.get(d) == 0)
            throw new RuntimeException("You don't have a " + d + " development!");
        developments.put(d, developments.get(d) - 1);
        for (Development e : Development.all()) {
            disabled.put(e, developments.get(e) + disabled.get(e));
            developments.put(e, 0);
        }
    }

    void reenable() {
        for (Development d : Development.all()) {
            developments.put(d, developments.get(d) + disabled.get(d));
            disabled.put(d, 0);
        }
    }

    public String toString() {
        String ans = "";
        for (Development d : Development.all())
            for (int i = 0, n = developments.get(d) + disabled.get(d); i < n; i++)
                ans += d.chr();
        return ans;
    }

}

