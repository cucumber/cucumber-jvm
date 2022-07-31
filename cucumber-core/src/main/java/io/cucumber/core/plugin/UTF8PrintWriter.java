package io.cucumber.core.plugin;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * A "good enough" PrintWriter implementation that writes UTF-8 and rethrows all
 * exceptions as runtime exceptions.
 */
final class UTF8PrintWriter implements Appendable, Closeable, Flushable {

    private final OutputStreamWriter out;

    UTF8PrintWriter(OutputStream out) {
        this.out = new UTF8OutputStreamWriter(out);
    }

    public void println() {
        try {
            out.write(System.lineSeparator());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void println(String s) {
        try {
            out.write(s);
            out.write(System.lineSeparator());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void flush() {
        try {
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Appendable append(CharSequence csq) {
        try {
            return out.append(csq);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) {
        try {
            return out.append(csq, start, end);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Appendable append(char c) {
        try {
            return out.append(c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
