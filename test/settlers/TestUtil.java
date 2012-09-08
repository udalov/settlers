package settlers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TestUtil {
    private TestUtil() {}

    public static List<String> readNonEmptyLines(String filename) {
        try {
            List<String> result = new ArrayList<String>();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(filename));
                String s;
                while ((s = br.readLine()) != null)
                    if (!s.isEmpty())
                        result.add(s);
            } finally {
                if (br != null)
                    br.close();
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Object getField(T object, Class<T> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            Object result = field.get(object);
            field.setAccessible(accessible);
            return result;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
