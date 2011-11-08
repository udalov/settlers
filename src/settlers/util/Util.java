package settlers.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.List;

public class Util {
    
    private Util() { }

    public static int numberOfOccurrences(char needle, String haystack) {
        int ans = 0;
        for (int i = 0; i < haystack.length(); i++)
            if (haystack.charAt(i) == needle)
                ans++;
        return ans;
    }

    public static <T> List<T> shuffle(List<T> list, Random rnd) {
        List<T> l = new ArrayList<T>(list);
        Collections.shuffle(l, rnd);
        return l;
    }

    public static <T> List<T> shuffle(List<T> list) {
        return shuffle(list, new Random());
    }

}

