package io.cucumber.core.plugin;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

final class UTF8OutputStreamWriter extends OutputStreamWriter {

    UTF8OutputStreamWriter(OutputStream out) {
        super(out, StandardCharsets.UTF_8);
    }

}
