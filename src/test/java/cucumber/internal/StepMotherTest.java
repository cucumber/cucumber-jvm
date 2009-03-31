package cucumber.internal;

import org.junit.Test;

public class StepMotherTest {
    @Test
    public void shouldInvokeSuccessfully() throws Throwable {
        StepMother mother = new StepMother();
        mother.add(CukeSteps.class);
        mother.newWorld();

        StepDefinition given = mother.getStepDefinitions().get(0);
        StepDefinition then = mother.getStepDefinitions().get(1);

        given.invokeOnTarget(new Object[]{"56", "green"});
        then.invokeOnTarget(new Object[]{"56", "green"});
    }

    @Test(expected=RuntimeException.class) 
    public void shouldInvokeWithFailure() throws Throwable {
        StepMother mother = new StepMother();
        mother.add(CukeSteps.class);
        mother.newWorld();

        StepDefinition given = mother.getStepDefinitions().get(0);
        StepDefinition then = mother.getStepDefinitions().get(1);

        given.invokeOnTarget(new Object[]{"56", "green"});
        then.invokeOnTarget(new Object[]{"99", "green"});
    }

    @Test
    public void shouldConvertLongs() throws Throwable {
        StepMother mother = new StepMother();
        mother.add(CukeSteps.class);
        mother.newWorld();

        StepDefinition given = mother.getStepDefinitions().get(2);
//        given.invokeOnTarget(new Object[]{"33"});
    }
}
