package cucumber.core2;

import cucumber.runtime.StepDefinition;
import cucumber.runtime.StubStepDefinition;
import org.junit.Test;
import pickles.Pickle;
import pickles.PickleStep;
import pickles.PickleTag;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class CompilerTest {

    public static final ArrayList<PickleTag> NO_TAGS = new ArrayList<PickleTag>();
    private int cukes;

    @Test
    public void builds_executable_test_case() throws NoSuchMethodException {
        Method some_cukes = getClass().getMethod("some_cukes", Integer.TYPE);
        StepDefinition stepdef = new StubStepDefinition(this, some_cukes, "I have (\\d+) cukes");
        List<StepDefinition> stepdefs = singletonList(stepdef);
        Compiler compiler = new Compiler(stepdefs);

        List<PickleStep> steps = singletonList(new PickleStep("I have 4 cukes", null));
        Pickle pickle = new Pickle("Scenario: test", steps, NO_TAGS);

        TestCase testCase = compiler.compile(pickle);
        testCase.run();

        assertEquals(4, cukes);
    }

    public void some_cukes(int cukes) {
        this.cukes = cukes;
    }
}