package cuke4duke.internal.clj;

import clojure.lang.AFunction;
import clojure.lang.Compiler;
import clojure.lang.RT;
import cuke4duke.internal.language.AbstractProgrammingLanguage;
import cuke4duke.internal.language.LanguageMixin;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.regex.Pattern;

public class CljLanguage extends AbstractProgrammingLanguage {
    private static CljLanguage instance;

    public CljLanguage(LanguageMixin languageMixin) throws Exception {
        super(languageMixin);
        instance = this;
        clearHooksAndStepDefinitions();
        RT.load("cuke4duke/internal/clj/clj_dsl");
    }

    public static void addCljStepDefinition(Pattern regexp, AFunction closure) throws Throwable {
        instance.addStepDefinition(new CljStepDefinition(instance, regexp, closure));
    }

    public static void addCljBeforeHook(AFunction closure) {
        instance.addBeforeHook(new CljHook(new String[0], closure));
    }


    public static void addCljAfterHook(AFunction closure) {
        instance.addAfterHook(new CljHook(new String[0], closure));
    }

    public void load_code_file(String cljFile) throws Throwable {
        Compiler.loadFile(cljFile);
    }

    protected void begin_scenario(IRubyObject scenario) throws Throwable {
    }

    public void end_scenario() throws Throwable {
    }

    @Override
    protected Object customTransform(Object arg, Class<?> parameterType) {
        return null;
    }
}
