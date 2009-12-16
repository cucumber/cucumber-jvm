package cuke4duke.internal.language;

import org.jruby.RubyArray;

public interface ProgrammingLanguage {
    void load_code_file(String file) throws Throwable;
    RubyArray step_matches(String step_name, String formatted_step_name) throws Throwable;
}
