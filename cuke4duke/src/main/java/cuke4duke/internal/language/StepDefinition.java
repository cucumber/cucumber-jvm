package cuke4duke.internal.language;

import org.jruby.RubyRegexp;
import org.jruby.RubyArray;

public interface StepDefinition {
    /**
     * @return the regexp, as a Ruby Regexp object.
     */
    RubyRegexp regexp();

    String file_colon_line();

    void invoke(RubyArray args) throws Throwable;
}
