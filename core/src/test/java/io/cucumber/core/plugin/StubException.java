package io.cucumber.core.plugin;

import java.io.PrintStream;
import java.io.PrintWriter;

class StubException extends RuntimeException {

    private final String stacktrace;

    StubException(String message, String stacktrace) {
        super(message);
        this.stacktrace = stacktrace;
    }

    public StubException() {
        this("message", "the stack trace");
    }

    @Override
    public void printStackTrace(PrintWriter printWriter) {
        printWriter.print(stacktrace);
    }

    @Override
    public void printStackTrace(PrintStream printStream) {
        printStream.print(stacktrace);
    }

}
