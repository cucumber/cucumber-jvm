package cuke4duke.internal.ik;

import cuke4duke.Scenario;
import cuke4duke.internal.language.AbstractProgrammingLanguage;
import cuke4duke.internal.language.LanguageMixin;
import cuke4duke.spi.ExceptionFactory;
import ioke.lang.IokeObject;
import ioke.lang.Runtime;
import ioke.lang.exceptions.ControlFlow;

import java.util.Locale;

public class IkLanguage extends AbstractProgrammingLanguage {
    private final Runtime ioke;
    final IokeObject pendingCondition;
    final IokeObject failedExpectationCondition;


    public IkLanguage(LanguageMixin languageMixin, ExceptionFactory exceptionFactory) throws Exception, ControlFlow {
        super(languageMixin, exceptionFactory);

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

    protected void begin_scenario(Scenario scenario) throws Throwable {
    }

    public void end_scenario() throws Throwable {
    }

    @Override
    protected Object customTransform(Object arg, Class<?> parameterType, Locale locale) {
        return null;
    }

}
