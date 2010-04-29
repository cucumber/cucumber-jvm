package cuke4duke.internal.language;

import cuke4duke.spi.jruby.StepMatch;

import java.util.List;

public interface LanguageMixin {
    void add_hook(String phase, Hook hook);

    void clear_hooks();

    StepMatch create_step_match(StepDefinition step_definition, String step_name, String formatted_step_name, List<StepArgument> step_arguments);

    void available_step_definition(String regexp_source, String file_colon_line);

    void invoked_step_definition(String regexp_source, String file_colon_line);
}
