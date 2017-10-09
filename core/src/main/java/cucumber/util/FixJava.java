package cucumber.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class FixJava {

    public static String join(List<String> strings, String separator) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String s : strings) {
            if (i != 0) sb.append(separator);
            sb.append(s);
            i++;
        }
        return sb.toString();
    }

    public static <T, R> List<R> map(List<T> objects, Mapper<T, R> mapper) {
        List<R> result = new ArrayList<R>(objects.size());
        for (T o : objects) {
            result.add(mapper.map(o));
        }
        return result;
    }

    public static String readResource(String resourcePath) throws RuntimeException {
        try {
            Reader reader = new InputStreamReader(FixJava.class.getResourceAsStream(resourcePath), "UTF-8");
            return readReader(reader);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readReader(Reader in) throws RuntimeException {
        try {
            StringBuilder buffer = new StringBuilder();
            final char[] data = new char[0x10000];
            int read;

            while ((read = in.read(data, 0, data.length)) != -1) {
                buffer.append(data, 0, read);
            }
            return buffer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readStream(InputStream in) throws RuntimeException {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final byte[] data = new byte[0x10000];
            int read;

            while ((read = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, read);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
