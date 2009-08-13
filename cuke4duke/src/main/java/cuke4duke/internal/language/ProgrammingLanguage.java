package cuke4duke.internal.language;

import org.jruby.runtime.builtin.IRubyObject;

import java.io.IOException;

public interface ProgrammingLanguage {
    void load_step_def_file(String step_def_file) throws Exception;
    void new_world(IRubyObject stepMother); // TODO: begin_scenario
    void nil_world(); // TODO: end_scenario
}
