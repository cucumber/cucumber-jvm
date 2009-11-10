package cuke4duke.internal.clj;

import clojure.lang.AFunction;
import cuke4duke.internal.language.Hook;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

@SuppressWarnings("serial")
public class CljHook extends AFunction implements Hook {
    @SuppressWarnings("unused")
    private final CljLanguage instance;
    private final AFunction closure;

    public CljHook(CljLanguage instance, AFunction closure) {
        this.instance = instance;
        this.closure = closure;
    }

    public RubyArray tag_names() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void invoke(String location, IRubyObject scenario) throws Throwable {
        closure.call();
    }
}
