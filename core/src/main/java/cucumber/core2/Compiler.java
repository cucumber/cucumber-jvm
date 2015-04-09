package cucumber.core2;

import cucumber.runtime.AmbiguousStepDefinitionsException;
import cucumber.runtime.Argument;
import cucumber.runtime.StepDefinition;
import pickles.Pickle;
import pickles.PickleStep;

import java.util.ArrayList;
import java.util.List;

public class Compiler {
    private final List<StepDefinition> stepdefs;

    public Compiler(List<StepDefinition> stepdefs) {
        this.stepdefs = stepdefs;
    }

    public TestCase compile(Pickle pickle) {
        List<TestStep> testSteps = new ArrayList<TestStep>();
        for (PickleStep pickleStep : pickle.getSteps()) {
            testSteps.add(compile(pickleStep));
        }
        return null;
    }

    private TestStep compile(PickleStep pickleStep) {
        List<Argument> arguments = null;
        for (StepDefinition stepdef : stepdefs) {
            List<Argument> argumentsCandidate = stepdef.matchedArguments(pickleStep.getText());
            if(argumentsCandidate != null) {
                if(arguments != null) {
                    throw new AmbiguousStepDefinitionsException(null);
                }
            }
        }
        return null;
    }
}
