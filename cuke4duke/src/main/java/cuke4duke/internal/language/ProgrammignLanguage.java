package cuke4duke.internal.language;

import org.jruby.RubyArray;

public interface ProgrammignLanguage {
    RubyArray step_matches(String step_name, String formatted_step_name);

    void load_code_file(String file) throws Throwable;

    void begin_scenario() throws Throwable;

    void end_scenario() throws Throwable;
}
