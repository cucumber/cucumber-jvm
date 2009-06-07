package cuke4duke.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public abstract class StepMotherTest {
    protected StepMother mother;

    @Test
    public void shouldInvokeSuccessfully() throws Throwable {
        mother.newWorld();

        StepDefinition given = mother.getStepDefinitions().get(0);
        StepDefinition then = mother.getStepDefinitions().get(1);

        given.invokeOnTarget(new Object[]{"56", "green"});
        then.invokeOnTarget(new Object[]{"56", "green"});
    }

    @Test(expected = RuntimeException.class)
    public void shouldInvokeWithFailure() throws Throwable {
        mother.newWorld();

        StepDefinition given = mother.getStepDefinitions().get(0);
        StepDefinition then = mother.getStepDefinitions().get(1);

        given.invokeOnTarget(new Object[]{"56", "green"});
        then.invokeOnTarget(new Object[]{"99", "green"});
    }

    @Test
    public void shouldConvertLongs() throws Throwable {
        mother.newWorld();

        StepDefinition given = mother.getStepDefinitions().get(2);
        given.invokeOnTarget(new Object[]{"33"});
    }

    @Test
    public void shouldCreateNewStepDefinitionsForEachNewWorld() throws Throwable {
        mother.newWorld();

        List<StepDefinition> stepDefs1 = mother.getStepDefinitions();
        assertEquals(4, stepDefs1.size());

        List<StepDefinition> oldSteps = new ArrayList<StepDefinition>();
        for (StepDefinition stepDefinition : stepDefs1) {
            oldSteps.add(stepDefinition);
        }

        mother.newWorld();
        List<StepDefinition> stepDefs2 = mother.getStepDefinitions();
        assertEquals(4, stepDefs2.size());

        for (int i = 0; i < 4; i++) {
            assertNotSame(oldSteps.get(i), stepDefs2.get(i));
        }
    }
}
