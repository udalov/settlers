package settlers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
}
