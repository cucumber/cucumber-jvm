package cuke4duke.spi.jruby;

import cuke4duke.internal.language.StepDefinition;

// TODO: Get rid of this interface - it's just here to please JRuby
public interface StepMatch {
    String file_colon_line();

    String backtrace_line();

    int text_length();

    StepDefinition step_definition();

    String name();

    String inspect();

    String format_args(Object format, Object proc);

    String format_args(Object format);

    String format_args();

    void invoke(Object format);
}
