package cuke4duke.internal.ik;

import cuke4duke.internal.language.LanguageMixin;
import cuke4duke.internal.language.ProgrammingLanguage;
import ioke.lang.Runtime;
import ioke.lang.IokeObject;
import ioke.lang.exceptions.ControlFlow;

public class IkLanguage extends ProgrammingLanguage {
    private final Runtime ioke;

    public IkLanguage(LanguageMixin languageMixin) throws Exception, ControlFlow {
        super(languageMixin);

        ioke = new Runtime();
        ioke.init();
        ioke.ground.setCell("CucumberLanguage", this);
        ioke.evaluateString("use(\"cuke4duke/internal/ik/ik_dsl\")");
        clearHooksAndStepDefinitions();
    }

    public void addIokeStepDefinition(Object iokeStepDefObject) {
        // Cast because of Ioke bug (?)
        addStepDefinition(new IkStepDefinition(this, ioke, (IokeObject) iokeStepDefObject));
    }

    public void load_code_file(String ikFile) throws Throwable {
        this.ioke.evaluateString("use(\"" + ikFile + "\")");
    }

    protected void prepareScenario() throws Throwable {
    }

    public void cleanupScenario() throws Throwable {
    }
}
