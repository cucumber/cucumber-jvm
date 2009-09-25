package cuke4duke.internal.language;

import cuke4duke.internal.JRuby;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.ArrayList;
import java.util.List;

public abstract class ProgrammingLanguage {
    protected final LanguageMixin languageMixin;
    private List<StepDefinition> stepDefinitions;
    private List<Hook> befores;
    private List<Hook> afters;

    public ProgrammingLanguage(LanguageMixin languageMixin) {
        this.languageMixin = languageMixin;
    }

    final public RubyArray step_matches(String step_name, String formatted_step_name) {
        return RubyArray.newArray(JRuby.getRuntime(), step_match_list(step_name, formatted_step_name));
    }

    public abstract void load_code_file(String file) throws Throwable;

    public final List<IRubyObject> step_match_list(String step_name, String formatted_step_name) {
        List<IRubyObject> matches = new ArrayList<IRubyObject>();
        for(StepDefinition stepDefinition : stepDefinitions){
            List<StepArgument> arguments = stepDefinition.arguments_from(step_name);
            if(arguments != null){
                matches.add(languageMixin.create_step_match(stepDefinition, step_name, formatted_step_name, arguments));
            }
        }
        return matches;
    }

    public final void begin_scenario() throws Throwable {
        prepareScenario();
        for(Hook before : befores){
            before.invoke("before", null);
        }
    }

    protected void clearHooksAndStepDefinitions() {
        befores = new ArrayList<Hook>();
        stepDefinitions = new ArrayList<StepDefinition>();
        afters = new ArrayList<Hook>();
    }

    public final void end_scenario() throws Throwable {
        for(Hook after : afters){
            after.invoke("after", null);
        }
        cleanupScenario();
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
}
