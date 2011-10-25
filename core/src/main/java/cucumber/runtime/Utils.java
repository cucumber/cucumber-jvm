package cucumber.runtime;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static <T> List<T> arrayOf(int size, T type) {
        List<T> list = new ArrayList<T>();
        for (int i = 0; i < size; i++) {
            list.add(type);
        }
        return list;
    }
}