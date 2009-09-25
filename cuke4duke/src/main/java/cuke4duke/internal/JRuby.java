package cuke4duke.internal;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.exceptions.RaiseException;

/**
 * Keeps a reference to the Ruby instance that was used to
 * start the whole machinery. I hate this static stuff, but
 * there is currently no clean way around it.
 */
public class JRuby {
    private static Ruby runtime;

    public static void setRuntime(Ruby runtime) {
        JRuby.runtime = runtime;
    }

    public static Ruby getRuntime() {
        if(runtime == null) {
            runtime = Ruby.getGlobalRuntime();
        }
        return runtime;
    }

    public static RaiseException cucumberPending(String message) {
        return error("Pending", message);
    }

    public static RaiseException cucumberArityMismatchError(String message) {
        return error("ArityMismatchError", message);
    }

    public static RaiseException cucumberUndefined(String message){
        return error("Undefined", message);
    }

    private static RaiseException error(String errorClass, String message) {
        RubyModule cucumber = getRuntime().getModule("Cucumber");
        RubyClass error = cucumber.getClass(errorClass);
        return new RaiseException(
                getRuntime(),
                error,
                message,
                true
        );
    }

}
