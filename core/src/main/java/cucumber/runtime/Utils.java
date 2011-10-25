package cucumber.runtime;

public class Utils {
    public static Class<?>[] classArray(int size, Class<?> clazz) {
        Class<?>[] arr = new Class<?>[size];
        for (int i = 0; i < size; i++) {
            arr[i] = clazz;
        }
        return arr;
    }
}