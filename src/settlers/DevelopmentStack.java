package settlers;

import java.util.HashMap;
import java.util.Map;

public class DevelopmentStack {

    private final Map<Development, Integer> developments =
        new HashMap<Development, Integer>();

    DevelopmentStack() {
        for (Development d : Development.class.getEnumConstants()) {
            developments.put(d, 0);
        }
    }

    public int howMany(Development d) { return developments.get(d); }

    public int size() {
        int ans = 0;
        for (Development d : Development.class.getEnumConstants()) {
            ans += developments.get(d);
        }
        return ans;
    }

    public int knight() { return howMany(Development.KNIGHT); }
    public int roadBuilding() { return howMany(Development.ROAD_BUILDING); }
    public int invention() { return howMany(Development.INVENTION); }
    public int monopoly() { return howMany(Development.MONOPOLY); }
    public int victoryPoint() { return howMany(Development.VICTORY_POINT); }

    void add(Development d) {
        developments.put(d, developments.get(d) + 1);
    }

    void sub(Development d) {
        if (howMany(d) == 0)
            throw new RuntimeException("You don't have a " + d + " development!");
        developments.put(d, developments.get(d) - 1);
    }

}

