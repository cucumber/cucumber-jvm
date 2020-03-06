package io.cucumber.core.plugin;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;

public class IO {
    public static OutputStream openStream(URL url) throws IOException {
        return url.getProtocol().equals("file") ? new FileOutputStream(url.getFile()) :
            new URLOutputStream(url);
    }

    public static Writer openWriter(URL url) throws IOException {
        return new UTF8OutputStreamWriter(openStream(url));
    }

    public static NiceAppendable openNiceAppendable(URL url) throws IOException {
        return new NiceAppendable(openWriter(url));
    }
}
