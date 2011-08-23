package cucumber.runtime;

public class Utils {
    public static Class<?>[] classArray(int size, Class<?> clazz) {
        Class<?>[] arr = new Class<?>[size];
        for (int i = 0; i < size; i++) {
            arr[i] = clazz;
        }
        return arr;
    }

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
}
