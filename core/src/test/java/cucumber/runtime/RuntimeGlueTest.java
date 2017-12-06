package cucumber.runtime;

import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuntimeGlueTest {

    private RuntimeGlue glue;

    @Before
    public void setUp() throws Exception {
        glue = new RuntimeGlue(new LocalizedXStreams(Thread.currentThread().getContextClassLoader()));
    }

    @Test
    public void throws_duplicate_error_on_dupe_stepdefs() {

        StepDefinition a = mock(StepDefinition.class);
        when(a.getPattern()).thenReturn("hello");
        when(a.getLocation(true)).thenReturn("foo.bf:10");
        glue.addStepDefinition(a);

        StepDefinition b = mock(StepDefinition.class);
        when(b.getPattern()).thenReturn("hello");
        when(b.getLocation(true)).thenReturn("bar.bf:90");
        try {
            glue.addStepDefinition(b);
            fail("should have failed");
        } catch (DuplicateStepDefinitionException expected) {
            assertEquals("Duplicate step definitions in foo.bf:10 and bar.bf:90", expected.getMessage());
        }
    }

    @Test
    public void removes_glue_that_is_scenario_scoped() {
        // This test is a bit fragile - it is testing state, not behaviour.
        // But it was too much hassle creating a better test without refactoring RuntimeGlue
        // and probably some of its immediate collaborators... Aslak.


        StepDefinition sd = mock(StepDefinition.class);
        when(sd.isScenarioScoped()).thenReturn(true);
        when(sd.getPattern()).thenReturn("pattern");
        glue.addStepDefinition(sd);

        HookDefinition bh = mock(HookDefinition.class);
        when(bh.isScenarioScoped()).thenReturn(true);
        glue.addBeforeHook(bh);

        HookDefinition ah = mock(HookDefinition.class);
        when(ah.isScenarioScoped()).thenReturn(true);
        glue.addAfterHook(ah);

        assertEquals(1, glue.stepDefinitionsByPattern.size());
        assertEquals(1, glue.beforeHooks.size());
        assertEquals(1, glue.afterHooks.size());

        glue.removeScenarioScopedGlue();

        assertEquals(0, glue.stepDefinitionsByPattern.size());
        assertEquals(0, glue.beforeHooks.size());
        assertEquals(0, glue.afterHooks.size());
    }

    @Test
    public void step_definition_match() {

        StepDefinition stepDefinition1 = getStepDefinitionMockWithPattern("pattern1");
        StepDefinition stepDefinition2 = getStepDefinitionMockWithPattern("^pattern2");
        StepDefinition stepDefinition3 = getStepDefinitionMockWithPattern("^pattern[1,3]");

        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        glue.addStepDefinition(stepDefinition3);

        String featurePath = "someFeature.feature";
        assertNull(glue.stepDefinitionMatch(featurePath, getPickleStep("pattern")));

        assertEquals("^pattern2",glue.stepDefinitionMatch(featurePath, getPickleStep("pattern2")).getPattern());
        assertEquals("^pattern2",glue.matchedStepDefinitionsCache.get("pattern2").stepDefinition.getPattern());
        assertTrue(glue.matchedStepDefinitionsCache.get("pattern2").arguments.isEmpty());
        assertEquals("^pattern2",glue.stepDefinitionMatch(featurePath, getPickleStep("pattern2")).getPattern());

        assertEquals("^pattern[1,3]",glue.stepDefinitionMatch(featurePath, getPickleStep("pattern3")).getPattern());

        boolean ambiguousCalled = false;
        try {

            glue.stepDefinitionMatch(featurePath, getPickleStep("pattern1"));
        } catch (AmbiguousStepDefinitionsException e) {
            assertEquals(2,e.getMatches().size());
            ambiguousCalled = true;
        }
        assertTrue(ambiguousCalled);
        ambiguousCalled = false;
        //try again to verify if we don't cache when there is duplicate step
        try {

            glue.stepDefinitionMatch(featurePath, getPickleStep("pattern1"));
        } catch (AmbiguousStepDefinitionsException e) {
            assertEquals(2,e.getMatches().size());
            ambiguousCalled = true;
        }
        assertTrue(ambiguousCalled);
    }

    private PickleStep getPickleStep(String text) {
        return new PickleStep(text, Collections.<Argument>emptyList(), Collections.<PickleLocation>emptyList());
    }

    private StepDefinition getStepDefinitionMockWithPattern(String pattern) {
        final JdkPatternArgumentMatcher jdkPatternArgumentMatcher = new JdkPatternArgumentMatcher(Pattern.compile(pattern));
        StepDefinition stepDefinition = mock(StepDefinition.class);
        when(stepDefinition.getPattern()).thenReturn(pattern);
        when(stepDefinition.matchedArguments(any(PickleStep.class))).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return jdkPatternArgumentMatcher.argumentsFrom(invocationOnMock.getArgumentAt(0,PickleStep.class).getText());
            }
        });
        return stepDefinition;
    }
}
