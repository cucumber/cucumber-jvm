package io.cucumber.core.backend;

import io.cucumber.core.backend.Pending;

import java.io.PrintStream;
import java.io.PrintWriter;

@Pending
public final class StubPendingException extends RuntimeException {

    public StubPendingException() {
        this("TODO: implement me");
    }

    public StubPendingException(String message) {
        super(message);
    }

    @Override
    public void printStackTrace(PrintWriter printWriter) {
        printWriter.print(getMessage());
    }

    @Override
    public void printStackTrace(PrintStream printStream) {
        printStream.print(getMessage());
    }

}
