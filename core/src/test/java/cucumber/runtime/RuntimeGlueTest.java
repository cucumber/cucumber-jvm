package cucumber.runtime;

import cucumber.runtime.xstream.LocalizedXStreams;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuntimeGlueTest {
    @Test
    public void throws_duplicate_error_on_dupe_stepdefs() {
        RuntimeGlue glue = new RuntimeGlue(new UndefinedStepsTracker(), new LocalizedXStreams(Thread.currentThread().getContextClassLoader()));

        StepDefinition a = mockStepDefinition("hello", "foo.bf:10");
        glue.addStepDefinition(a);

        StepDefinition b = mockStepDefinition("hello", "bar.bf:90");
        try {
            glue.addStepDefinition(b);
            fail("should have failed");
        } catch (DuplicateStepDefinitionException expected) {
            assertEquals("Duplicate step definitions in foo.bf:10 and bar.bf:90", expected.getMessage());
        }
    }

    @Test
    public void does_not_throw_duplicate_error_on_dupe_stepdefs_with_identical_locations() {
        RuntimeGlue glue = new RuntimeGlue(new UndefinedStepsTracker(), new LocalizedXStreams(Thread.currentThread().getContextClassLoader()));

        StepDefinition a = mockStepDefinition("hello", "foo.bf:10");
        glue.addStepDefinition(a);

        StepDefinition b = mockStepDefinition("hello", "foo.bf:10");
        glue.addStepDefinition(b);
    }

    private StepDefinition mockStepDefinition(String pattern, String location) {
        StepDefinition stepDefinition = mock(StepDefinition.class);
        when(stepDefinition.getPattern()).thenReturn(pattern);
        when(stepDefinition.getLocation(true)).thenReturn(location);
        return stepDefinition;
    }

}
