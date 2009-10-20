package cuke4duke.internal.language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

import cuke4duke.internal.JRuby;

public abstract class AbstractProgrammingLanguage implements ProgrammingLanguage {
    protected final LanguageMixin languageMixin;
    private List<StepDefinition> stepDefinitions;
    private List<Hook> befores;
    private List<Hook> afters;
    private Map<Class<?>, Transformable> transforms = new HashMap<Class<?>, Transformable>();

    public AbstractProgrammingLanguage(LanguageMixin languageMixin) {
        this.languageMixin = languageMixin;
    }

    final public RubyArray step_matches(String step_name, String formatted_step_name) throws Throwable {
        return JRuby.newArray(step_match_list(step_name, formatted_step_name));
    }

    public abstract void load_code_file(String file) throws Throwable;

    public final List<IRubyObject> step_match_list(String step_name, String formatted_step_name) throws Throwable {
        List<IRubyObject> matches = new ArrayList<IRubyObject>();
        for (StepDefinition stepDefinition : stepDefinitions) {
            List<StepArgument> arguments = stepDefinition.arguments_from(step_name);
            if (arguments != null) {
                matches.add(languageMixin.create_step_match(stepDefinition, step_name, formatted_step_name, arguments));
            }
        }
        return matches;
    }

    public final void begin_scenario() throws Throwable {
        prepareScenario();
        for (Hook before : befores) {
            before.invoke("before", null);
        }
    }

    protected void clearHooksAndStepDefinitions() {
//        transforms = new HashMap<Class<?>, Transformable>();
        befores = new ArrayList<Hook>();
        stepDefinitions = new ArrayList<StepDefinition>();
        afters = new ArrayList<Hook>();
    }

    public final void end_scenario() throws Throwable {
        for (Hook after : afters) {
            after.invoke("after", null);
        }
        cleanupScenario();
    }

    public final Map<Class<?>, Transformable> getTransforms() {
        return transforms;
    }

    public void addTransform(Class<?> type, Transformable transform) {
        this.transforms.put(type, transform);
    }

    public void addBeforeHook(Hook before) {
        befores.add(before);
    }

    public void addStepDefinition(StepDefinition stepDefinition) {
        stepDefinitions.add(stepDefinition);
    }

    public void addAfterHook(Hook after) {
        afters.add(after);
    }

    protected abstract void prepareScenario() throws Throwable;

    public abstract void cleanupScenario() throws Throwable;

    public void availableStepDefinition(String regexp_source, String file_colon_line) {
        languageMixin.available_step_definition(regexp_source, file_colon_line);
    }

    public void invokedStepDefinition(String regexp_source, String file_colon_line) {
        languageMixin.invoked_step_definition(regexp_source, file_colon_line);
    }
}
