package cuke4duke.internal.clj;

import clojure.lang.AFunction;
import clojure.lang.Compiler;
import clojure.lang.RT;
import cuke4duke.internal.language.AbstractProgrammingLanguage;
import cuke4duke.internal.language.LanguageMixin;

import java.util.regex.Pattern;

public class CljLanguage extends AbstractProgrammingLanguage {
    private static CljLanguage instance;

    public CljLanguage(LanguageMixin languageMixin) throws Exception {
        super(languageMixin);
        instance = this;
        clearHooksAndStepDefinitions();
        RT.load("cuke4duke/internal/clj/clj_dsl");
    }

    public static void addStepDefinition(Pattern regexp, AFunction closure) throws Throwable {
        instance.addStepDefinition(new CljStepDefinition(instance, regexp, closure));
    }

    public void load_code_file(String cljFile) throws Throwable {
        Compiler.loadFile(cljFile);
    }

    protected void prepareScenario() throws Throwable {
    }

    public void cleanupScenario() throws Throwable {
    }
}
