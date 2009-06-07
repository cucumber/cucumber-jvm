package cuke4duke.internal;

import cuke4duke.app.PicoContainerHelloService;
import cuke4duke.steps.PicoContainerSteps;
import org.junit.Before;

public class PicoContainerStepMotherTest extends StepMotherTest {
    @Before
    public void createStepMother() {
        mother = new PicoContainerStepMother();
        mother.registerClass(PicoContainerHelloService.class);
        mother.registerClass(PicoContainerSteps.class);
    }
}
