package io.cucumber.core.util;

import java.io.IOException;
import java.io.Reader;

public class FixJava {

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
