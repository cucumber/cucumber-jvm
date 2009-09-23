package cuke4duke.internal.clj;

import cuke4duke.internal.language.ProgrammingLanguage;
import cuke4duke.internal.language.LanguageMixin;
import clojure.lang.Compiler;
import clojure.lang.RT;

public class CljLanguage extends ProgrammingLanguage {
    public CljLanguage(LanguageMixin languageMixin) throws Exception {
        super(languageMixin);
        RT.load("cuke4duke/internal/clj/clj_dsl");
    }

    public void begin_scenario() {
        System.out.println("begin_scenario");
    }

    public void end_scenario() {
        System.out.println("end_scenario");
    }

    protected void load(String file) throws Throwable {
        Compiler.loadFile(file);
    }
}
