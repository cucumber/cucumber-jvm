package cucumber.runtime;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;

public class Utils {
    public static Class<?>[] classArray(int size, Class<?> clazz) {
        Class<?>[] arr = new Class<?>[size];
        for (int i = 0; i < size; i++) {
            arr[i] = clazz;
        }
        return arr;
    }

    public static void closeQuietly(Reader input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    public static void closeQuietly(OutputStream output) {
        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }
}