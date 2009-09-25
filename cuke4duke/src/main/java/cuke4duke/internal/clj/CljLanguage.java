package cuke4duke.internal.clj;

import cuke4duke.internal.language.ProgrammingLanguage;
import cuke4duke.internal.language.LanguageMixin;
import cuke4duke.internal.language.StepDefinition;
import clojure.lang.Compiler;
import clojure.lang.RT;
import clojure.lang.AFunction;

import java.util.regex.Pattern;
import java.util.List;

import org.jruby.runtime.builtin.IRubyObject;

public class CljLanguage extends ProgrammingLanguage {
    private static CljLanguage instance;

    public CljLanguage(LanguageMixin languageMixin) throws Exception {
        super(languageMixin);
        instance = this;
        RT.load("cuke4duke/internal/clj/clj_dsl");
    }

    public static void addStepDefinition(Pattern regexp, AFunction closure) throws Exception {
        instance.createAndAddStepDefinition(regexp, closure);
    }

    public void createAndAddStepDefinition(Pattern regexp, AFunction closure) throws Exception {
        StepDefinition stepDefinition = new CljStepDefinition(regexp, closure);
        //addStepDefinition(stepDefinition);
    }

    public void begin_scenario() {
    }

    public void end_scenario() {
    }

    public void load_code_file(String file) throws Throwable {
        Compiler.loadFile(file);
    }

    @Override
    public List<IRubyObject> step_match_list(String step_name, String formatted_step_name) {
        throw new UnsupportedOperationException("Fixme");
    }
}
