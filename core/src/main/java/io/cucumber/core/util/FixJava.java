package io.cucumber.core.util;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class FixJava {

    private FixJava() {

    }

    public static String join(List<? extends Object> objects, String separator) {
        //TODO: Java8 replace with StringJoiner.
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object s : objects) {
            if (i != 0) sb.append(separator);
            sb.append(s);
            i++;
        }
        return sb.toString();
    }

    public static <T, R> List<R> map(List<T> objects, Mapper<T, R> mapper) {
        //TODO: Java8 replace with lambda
        List<R> result = new ArrayList<R>(objects.size());
        for (T o : objects) {
            result.add(mapper.map(o));
        }
        return result;
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

}
