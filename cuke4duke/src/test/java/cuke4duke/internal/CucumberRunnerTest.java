package cuke4duke.internal;

import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.apache.bsf.BSFException;
import cuke4duke.junit.JunitCukeSteps;

public class CucumberRunnerTest {
    @Test
    public void dummy() {}

    @Test
    public void shouldBeAbleToRunSingleFeature() throws BSFException {
        Visitor visitor = mock(Visitor.class);
        StepMother stepMother = new PicoContainerStepMother();
        stepMother.registerClass(JunitCukeSteps.class);

        CucumberRunner runner = new CucumberRunner(stepMother);
        runner.run("/demo.feature", "3 green and 4 yellow cukes", visitor);

        verify(visitor).visitFeatures();
        verify(visitor).visitScenarioName("Scenario:", "3 green and 4 yellow cukes");
    }
}
