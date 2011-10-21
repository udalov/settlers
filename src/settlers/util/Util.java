package settlers.util;

public class Util {
    
    private Util() { }

    public static int numberOfOccurances(char needle, String haystack) {
        int ans = 0;
        for (int i = 0; i < haystack.length; i++)
            if (haystack.charAt(i) == needle)
                ans++;
        return ans;
    }

}

