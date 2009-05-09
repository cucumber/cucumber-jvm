package cucumber.internal;

import cucumber.app.PicoContainerHelloService;
import cucumber.steps.PicoContainerSteps;
import org.junit.Before;

public class PicoContainerStepMotherTest extends StepMotherTest {
    @Before
    public void createStepMother() {
        mother = new PicoContainerStepMother();
        mother.registerClass(PicoContainerHelloService.class);
        mother.registerClass(PicoContainerSteps.class);
    }
}
