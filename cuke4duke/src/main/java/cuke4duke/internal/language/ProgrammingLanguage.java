package cuke4duke.internal.language;

import cuke4duke.spi.jruby.StepMatch;

import java.util.List;

public interface ProgrammingLanguage {
    void load_code_file(String file) throws Throwable;
    List<StepMatch> step_matches(String step_name, String formatted_step_name) throws Throwable;

    Exception cucumberPending(String message);
    Exception cucumberArityMismatchError(String message);
    Exception error(String type, String message);
}
