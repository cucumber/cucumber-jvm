package io.cucumber.core.plugin;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static io.cucumber.core.plugin.BytesEqualTo.isBytesEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;

class UTF8PrintWriterTest {

    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    final UTF8PrintWriter out = new UTF8PrintWriter(bytes);

    @Test
    void println() {
        out.println();
        out.println("Hello ");
        out.close();
        assertThat(bytes, isBytesEqualTo(
            System.lineSeparator() + "Hello " + System.lineSeparator()));
    }

    @Test
    void append() {
        out.append("Hello");
        out.append("Hello World", 5, 11);
        out.close();
        assertThat(bytes, isBytesEqualTo("Hello World"));
    }

    @Test
    void flush() {
        out.append("Hello");
        assertThat(bytes, isBytesEqualTo(""));
        out.flush();
        assertThat(bytes, isBytesEqualTo("Hello"));
    }

    @Test
    void close() {
        out.append("Hello");
        assertThat(bytes, isBytesEqualTo(""));
        out.close();
        assertThat(bytes, isBytesEqualTo("Hello"));
    }

}
