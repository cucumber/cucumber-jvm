package cuke4duke.internal;

public class Utils {
    public static String join(Object[] objects, String separator) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object o : objects) {
            if (i != 0) sb.append(separator);
            sb.append(o);
            i++;
        }
        return sb.toString();
    }

    public static Class<?>[] objectClassArray(int n) {
        Class<?>[] arr = new Class<?>[n];
        for(int i = 0; i < n; i++) {
            arr[i] = Object.class;
        }
        return arr;
    }
}
