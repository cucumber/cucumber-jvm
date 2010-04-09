package cuke4duke.internal.clj;

import clojure.lang.AFunction;
import clojure.lang.Compiler;
import clojure.lang.RT;
import cuke4duke.Scenario;
import cuke4duke.internal.language.AbstractProgrammingLanguage;
import cuke4duke.internal.language.LanguageMixin;
import cuke4duke.spi.ExceptionFactory;

import java.util.Locale;
import java.util.regex.Pattern;

public class CljLanguage extends AbstractProgrammingLanguage {
    private static CljLanguage instance;

    public CljLanguage(LanguageMixin languageMixin, ExceptionFactory exceptionFactory) throws Exception {
        super(languageMixin, exceptionFactory);
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

    protected void begin_scenario(Scenario scenario) throws Throwable {
    }

    public void end_scenario() throws Throwable {
    }

    @Override
    protected Object customTransform(Object arg, Class<?> parameterType, Locale locale) {
        return null;
    }
}
