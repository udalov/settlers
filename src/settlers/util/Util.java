package settlers.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.List;
import settlers.Resource;

public class Util {
    
    private Util() { }

    public static int numberOfOccurrences(char needle, String haystack) {
        int ans = 0;
        for (int i = 0; i < haystack.length(); i++)
            if (haystack.charAt(i) == needle)
                ans++;
        return ans;
    }

    public static boolean resourceString(String s) {
        i: for (int i = 0, n = s.length(); i < n; i++) {
            for (Resource r : Resource.all())
                if (r.chr() == s.charAt(i))
                    continue i;
            return false;
        }
        return true;
    }

    public static String toResourceString(Collection<Resource> resources) {
        StringBuilder sb = new StringBuilder();
        for (Resource r : resources)
            sb.append(r.chr());
        return sb + "";
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

