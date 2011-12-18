package settlers.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.List;
import settlers.Resource;
import settlers.GameException;

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

    public static <T> List<T> filter(List<T> list, Filter<T> filter) {
        List<T> ans = new ArrayList<T>();
        for (T obj : list)
            if (filter.filter(obj))
                ans.add(obj);
        return ans;
    }

    public static void checkBotName(String name) {
        if (name == null)
            throw new GameException("Name of your bot cannot be null");
        if ("".equals(name))
            throw new GameException("Name of your bot cannot be empty");
        if (name.length() > 64)
            throw new GameException("Name of your bot cannot be that long");
        for (char c : name.toCharArray())
            if ((int)c < 32 || 126 < (int)c)
                throw new GameException("Name of your bot can contain only characters with ASCII codes from 32 to 126");
    }

}

