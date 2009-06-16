package cuke4duke.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

public abstract class StepMotherTest {
    protected StepMother mother;

    @Test
    public void shouldInvokeSuccessfully() throws Throwable {
        mother.newWorld();

        StepDefinition given = mother.getStepDefinition("I have (\\d+) (.*) cukes");
        StepDefinition then = mother.getStepDefinition("I should have (\\d+) (.*) cukes");

        given.invokeOnTarget(new Object[]{"56", "green"});
        then.invokeOnTarget(new Object[]{"56", "green"});
    }

    @Test(expected = RuntimeException.class)
    public void shouldInvokeWithFailure() throws Throwable {
        mother.newWorld();

        StepDefinition given = mother.getStepDefinition("I have (\\d+) (.*) cukes");
        StepDefinition then = mother.getStepDefinition("I should have (\\d+) (.*) cukes");

        given.invokeOnTarget(new Object[]{"56", "green"});
        then.invokeOnTarget(new Object[]{"99", "green"});
    }

    @Test
    public void shouldConvertLongs() throws Throwable {
        mother.newWorld();

        StepDefinition given = mother.getStepDefinition("Longs: (\\d+)");
        given.invokeOnTarget(new Object[]{"33"});
    }

    @Test
    public void shouldCreateNewStepDefinitionsForEachNewWorld() throws Throwable {
        mother.newWorld();

        Collection<StepDefinition> stepDefs1 = mother.getStepDefinitions();
        assertEquals(4, stepDefs1.size());
        List<StepDefinition> oldSteps = new ArrayList<StepDefinition>(stepDefs1);

        mother.newWorld();
        Collection<StepDefinition> stepDefs2 = mother.getStepDefinitions();
        assertEquals(4, stepDefs2.size());
        List<StepDefinition> newSteps = new ArrayList<StepDefinition>(stepDefs1);

        for (int i = 0; i < 4; i++) {
            assertNotSame(oldSteps.get(i), newSteps.get(i));
        }
    }
}
