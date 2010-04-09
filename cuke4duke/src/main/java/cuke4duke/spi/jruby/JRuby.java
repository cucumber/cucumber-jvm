package cuke4duke.spi.jruby;

import org.jruby.Ruby;
import org.jruby.RubyArray;

import java.util.Collection;

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

    public static RubyArray newArray(Collection<?> collection) {
        RubyArray result = RubyArray.newArray(getRuntime());
        for (Object o : collection) {
            result.add(o);
        }
        return result;
    }
}
