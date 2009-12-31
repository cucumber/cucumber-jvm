package cuke4duke.internal.clj;

import clojure.lang.AFunction;
import cuke4duke.internal.language.AbstractHook;
import org.jruby.runtime.builtin.IRubyObject;

public class CljHook extends AbstractHook {
    private final AFunction closure;

    public CljHook(String[] tagExpressions, AFunction closure) {
        super(tagExpressions);
        this.closure = closure;
    }

    public void invoke(String location, IRubyObject scenario) throws Throwable {
        closure.call();
    }
}
