package io.cucumber.core.plugin;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * A nice appendable that doesn't throw checked exceptions
 */
final class NiceAppendable implements Appendable {

    private static final CharSequence NL = "\n";
    private final Appendable out;

    public NiceAppendable(Appendable out) {
        this.out = out;
    }

    public NiceAppendable println() {
        return append(NL);
    }

    public NiceAppendable append(CharSequence csq) {
        try {
            out.append(csq);
            tryFlush();
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NiceAppendable append(CharSequence csq, int start, int end) {
        try {
            out.append(csq, start, end);
            tryFlush();
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NiceAppendable append(char c) {
        try {
            out.append(c);
            tryFlush();
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void tryFlush() {
        if (!(out instanceof Flushable)) {
            return;
        }

        try {
            ((Flushable) out).flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NiceAppendable println(CharSequence csq) {
        try {
            out.append(csq).append(NL);
            tryFlush();
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            tryFlush();
            if (out instanceof Closeable) {
                ((Closeable) out).close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
