package io.cucumber.core.plugin;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static io.cucumber.core.plugin.BytesEqualTo.isBytesEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class NiceAppendableTest {

    @Test
    public void should_flush_every_call_if_configured() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter writer = spy(new OutputStreamWriter(out));
        NiceAppendable appendable = new NiceAppendable(writer, true);

        appendable
                .append("First String,")
                .append("__Second String__", 2, 15)
                .append("\n")
                .println("Second line")
                .println()
                .close();

        assertThat(out, isBytesEqualTo("First String,Second String\nSecond line\n\n"));
        verify(writer, times(6)).flush(); // Each method call flushes
    }

    @Test
    public void should_not_flush_unless_configured() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter writer = spy(new OutputStreamWriter(out));
        NiceAppendable appendable = new NiceAppendable(writer);

        appendable
                .append("First String,")
                .append("__Second String__", 2, 15)
                .append("\n")
                .println("Second line")
                .println()
                .close();

        assertThat(out, isBytesEqualTo("First String,Second String\nSecond line\n\n"));
        verify(writer, times(0)).flush();
    }

}
