package cuke4duke.internal;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.RubyClass;
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

    public static void raisePending(String message) {
        RubyModule cucumber = getRuntime().getModule("Cucumber");
        RubyClass pending = cucumber.getClass("Pending");
        throw new RaiseException(
                getRuntime(),
                pending,
                message,
                true
        );
    }
}
