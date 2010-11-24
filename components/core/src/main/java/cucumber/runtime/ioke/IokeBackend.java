package cucumber.runtime.ioke;

import cucumber.runtime.*;
import ioke.lang.IokeObject;
import ioke.lang.Runtime;
import ioke.lang.exceptions.ControlFlow;

import java.util.ArrayList;
import java.util.List;

public class IokeBackend implements Backend {
    private final Runtime ioke;
    private final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private String currentLocation;

    final IokeObject pendingCondition;
    final IokeObject failedExpectationCondition;


    public IokeBackend(String scriptPath) {
        try {
            ioke = new Runtime();
            ioke.init();
            ioke.ground.setCell("IokeBackend", this);
            ioke.evaluateString("use(\"cucumber/runtime/ioke/dsl\")");
//        clearHooksAndStepDefinitions();

            pendingCondition = IokeObject.as(IokeObject.getCellChain(ioke.condition,
                    ioke.message,
                    ioke.ground,
                    "Pending"), ioke.ground);

            failedExpectationCondition = IokeObject.as(IokeObject.getCellChain(ioke.condition,
                    ioke.message,
                    ioke.ground,
                    "ISpec",
                    "ExpectationNotMet"), ioke.ground);
            Classpath.scan(scriptPath, ".ik", new Consumer() {
                public void consume(Input input) {
                    try {
                        currentLocation = input.getPath();
                        ioke.evaluateString("use(\"" + input.getPath() + "\")");
                    } catch (ControlFlow controlFlow) {
                        throw new CucumberException("Failed to load " + input.getPath(), controlFlow);
                    }
                }
            });

        } catch (Throwable e) {
            throw new CucumberException("Failed to initialize Ioke", e);
        }
    }

    public void addStepDefinition(Object iokeStepDefObject) throws Throwable {
        stepDefinitions.add(new IokeStepDefinition(this, ioke, (IokeObject) iokeStepDefObject, currentLocation));
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    public void newScenario() {
    }
}
