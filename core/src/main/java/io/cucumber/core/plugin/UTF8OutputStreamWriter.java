package io.cucumber.core.plugin;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

final class UTF8OutputStreamWriter extends OutputStreamWriter {
    UTF8OutputStreamWriter(OutputStream out) {
        super(out, Charset.forName("UTF-8"));
    }
}
