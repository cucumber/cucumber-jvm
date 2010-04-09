package cuke4duke.spi.jruby;

import cuke4duke.spi.ExceptionFactory;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.exceptions.RaiseException;

public class JRubyExceptionFactory implements ExceptionFactory {
    public Exception error(String errorClass, String message) {
        RubyModule cucumber = JRuby.getRuntime().getModule("Cucumber");
        RubyClass error = cucumber.getClass(errorClass);
        return new RaiseException(
                JRuby.getRuntime(),
                error,
                message,
                true
        );
    }

    public Exception cucumberPending(String message) {
        return error("Pending", message);
    }

    public Exception cucumberArityMismatchError(String message) {
        return error("ArityMismatchError", message);
    }
}
