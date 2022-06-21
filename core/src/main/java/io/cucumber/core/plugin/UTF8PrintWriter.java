package io.cucumber.core.plugin;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

final class UTF8PrintWriter extends PrintWriter {

    UTF8PrintWriter(OutputStream out) {
        super(out, false, StandardCharsets.UTF_8);
    }

}
