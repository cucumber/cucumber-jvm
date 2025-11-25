package io.cucumber.core.plugin;

import java.io.PrintStream;
import java.io.PrintWriter;

class StubException extends RuntimeException {

    private final String stacktrace;
    private final String className;

    private StubException(String className, String message, String stacktrace) {
        super(message);
        this.className = className;
        this.stacktrace = stacktrace;
    }

    StubException() {
        this("stub exception");
    }

    StubException(String message) {
        this(null, message, null);
    }

    StubException withClassName() {
        return new StubException(StubException.class.getName(), getMessage(), stacktrace);
    }

    StubException withStacktrace(String stacktrace) {
        return new StubException(className, getMessage(), stacktrace);
    }

    @Override
    public void printStackTrace(PrintWriter writer) {
        printStackTrace(new PrintWriterOrStream(writer));
    }

    @Override
    public void printStackTrace(PrintStream stream) {
        printStackTrace(new PrintWriterOrStream(stream));
    }

    private void printStackTrace(PrintWriterOrStream p) {
        if (className != null) {
            p.println(className);
        }
        p.print(getMessage());
        if (stacktrace != null) {
            p.println("");
            p.println("\t" + stacktrace);
        }
    }

    private static final class PrintWriterOrStream {
        private final PrintWriter writer;
        private final PrintStream stream;

        private PrintWriterOrStream(PrintWriter writer) {
            this.writer = writer;
            this.stream = null;
        }

        private PrintWriterOrStream(PrintStream stream) {
            this.writer = null;
            this.stream = stream;
        }

        void println(String s) {
            if (writer != null) {
                writer.println(s);
            }
            if (stream != null) {
                stream.println(s);
            }
        }

        void print(String s) {
            if (writer != null) {
                writer.print(s);
            }
            if (stream != null) {
                stream.print(s);
            }
        }
    }

}
