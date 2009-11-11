package cuke4duke.internal.ik;

import cuke4duke.internal.language.AbstractProgrammingLanguage;
import cuke4duke.internal.language.LanguageMixin;
import ioke.lang.IokeObject;
import ioke.lang.Runtime;
import ioke.lang.exceptions.ControlFlow;
import org.jruby.runtime.builtin.IRubyObject;

public class IkLanguage extends AbstractProgrammingLanguage {
    private final Runtime ioke;
    final IokeObject pendingCondition;
    final IokeObject failedExpectationCondition;


    public IkLanguage(LanguageMixin languageMixin) throws Exception, ControlFlow {
        super(languageMixin);

        ioke = new Runtime();
        ioke.init();
        ioke.ground.setCell("CucumberLanguage", this);
        ioke.evaluateString("use(\"cuke4duke/internal/ik/ik_dsl\")");
        clearHooksAndStepDefinitions();

        pendingCondition = IokeObject.as(IokeObject.getCellChain(ioke.condition,
                                                                 ioke.message,
                                                                 ioke.ground,
                                                                 "Pending"), ioke.ground);

        failedExpectationCondition = IokeObject.as(IokeObject.getCellChain(ioke.condition,
                                                                           ioke.message,
                                                                           ioke.ground,
                                                                           "ISpec",
                                                                           "ExpectationNotMet"), ioke.ground);
    }

    public void addIokeStepDefinition(Object iokeStepDefObject) throws Throwable {
        // Cast because of Ioke bug (?)
        addStepDefinition(new IkStepDefinition(this, ioke, (IokeObject) iokeStepDefObject));
    }

    public void load_code_file(String ikFile) throws Throwable {
        this.ioke.evaluateString("use(\"" + ikFile + "\")");
    }

    protected void begin_scenario(IRubyObject scenario) throws Throwable {
    }

    public void end_scenario() throws Throwable {
    }
}
