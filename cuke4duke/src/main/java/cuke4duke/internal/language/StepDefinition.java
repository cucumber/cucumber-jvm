package cuke4duke.internal.language;

import org.jruby.RubyArray;
import org.jruby.RubyRegexp;

public interface StepDefinition {
    /**
     * @return the regexp, as a Ruby Regexp object.
     */
    RubyRegexp regexp();

    String file_colon_line();

    void invoke(RubyArray args) throws Throwable;
}
