package io.cucumber.compatibility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class Resources {

    private Resources() {
        // utility class
    }

    public static byte[] read(String name) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream is = Resources.class.getResourceAsStream(name)) {
            int read;
            byte[] data = new byte[4096];
            while ((read = is.read(data, 0, data.length)) != -1) {
                bytes.write(data, 0, read);
            }
        }
        return bytes.toByteArray();
    }
}
