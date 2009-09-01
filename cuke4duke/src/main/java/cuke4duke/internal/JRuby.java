package cuke4duke.internal;

import org.jruby.Ruby;

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
}
