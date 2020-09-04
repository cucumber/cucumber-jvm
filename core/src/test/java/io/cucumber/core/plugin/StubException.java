package io.cucumber.core.plugin;

import java.io.PrintWriter;

class StubException extends RuntimeException {

    private final String stacktrace;

    StubException(String message, String stacktrace) {
        super(message);
        this.stacktrace = stacktrace;
    }

    @Override
    public void printStackTrace(PrintWriter printWriter) {
        printWriter.print(stacktrace);
    }

}
