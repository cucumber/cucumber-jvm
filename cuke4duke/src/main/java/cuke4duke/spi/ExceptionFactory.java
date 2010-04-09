package cuke4duke.spi;

public interface ExceptionFactory {
    Exception error(String errorClass, String message);

    Exception cucumberPending(String message);

    Exception cucumberArityMismatchError(String message);
}
