package settlers.util;

public class Pair<T, U> {
    
    public final T first;
    public final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public static <T, U> Pair<T, U> make(T first, U second) {
        return new Pair<T, U>(first, second);
    }

    public int hashCode() {
        int h1 = first == null ? 0 : first.hashCode();
        int h2 = second == null ? 0 : second.hashCode();
        return h1 ^ (h2 << 1);
    }

    public boolean equals(Object o) {
        if (!(o instanceof Pair))
            return false;
        Pair<?, ?> p = (Pair<?, ?>)o;
        return
            (first == null ? p.first == null : first.equals(p.first)) &&
            (second == null ? p.second == null : second.equals(p.second));
    }

}

