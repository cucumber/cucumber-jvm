package cuke4duke.internal.language;

import org.jruby.runtime.builtin.IRubyObject;

public interface Hook {
    public String[] tag_names();
    void invoke(String location, IRubyObject scenario) throws Throwable;
}
