package cuke4duke.internal.clj;

import clojure.lang.AFunction;
import cuke4duke.internal.language.AbstractHook;
import org.jruby.runtime.builtin.IRubyObject;

public class CljHook extends AbstractHook {
    private final AFunction closure;

    public CljHook(String[] tagNames, AFunction closure) {
        super(tagNames);
        this.closure = closure;
    }

    public void invoke(String location, IRubyObject scenario) throws Throwable {
        closure.call();
    }
}
