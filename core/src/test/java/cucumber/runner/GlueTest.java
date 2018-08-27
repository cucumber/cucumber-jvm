package cucumber.runner;

import cucumber.runtime.DuplicateStepDefinitionException;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import io.cucumber.stepexpression.ArgumentMatcher;
import io.cucumber.stepexpression.ExpressionArgumentMatcher;
import io.cucumber.stepexpression.StepExpression;
import io.cucumber.stepexpression.StepExpressionFactory;
import io.cucumber.stepexpression.TypeRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;

import static java.util.Locale.ENGLISH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GlueTest {

    private Glue glue;

    @Before
    public void setUp() {
        glue = new Glue();
    }

    @Test
    public void throws_duplicate_error_on_dupe_stepdefs() {
        Glue glue = new Glue();

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
    public void removes_scenario_scoped_cache_entries() {
        StepDefinition sd = getStepDefinitionMockWithPattern("pattern");
        when(sd.isScenarioScoped()).thenReturn(true);
        glue.addStepDefinition(sd);
        String featurePath = "someFeature.feature";

        String stepText = "pattern";
        PickleStep pickleStep1 = getPickleStep(stepText);
        assertEquals(sd, glue.stepDefinitionMatch(featurePath, pickleStep1).getStepDefinition());

        assertEquals(1, glue.matchedStepDefinitionsCache.size());

        glue.removeScenarioScopedGlue();

        assertEquals(0, glue.matchedStepDefinitionsCache.size());
    }

    @Test
    public void returns_null_if_no_matching_steps_found() {
        StepDefinition stepDefinition = getStepDefinitionMockWithPattern("pattern1");
        glue.addStepDefinition(stepDefinition);
        String featurePath = "someFeature.feature";

        PickleStep pickleStep = getPickleStep("pattern");
        assertNull(glue.stepDefinitionMatch(featurePath, pickleStep));
        verify(stepDefinition).matchedArguments(pickleStep);
    }

    @Test
    public void returns_match_from_cache_if_single_found() {
        StepDefinition stepDefinition1 = getStepDefinitionMockWithPattern("^pattern1");
        StepDefinition stepDefinition2 = getStepDefinitionMockWithPattern("^pattern2");
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        String featurePath = "someFeature.feature";
        String stepText = "pattern1";

        PickleStep pickleStep1 = getPickleStep(stepText);
        assertEquals(stepDefinition1, glue.stepDefinitionMatch(featurePath, pickleStep1).getStepDefinition());
        //verify if all defs are checked
        verify(stepDefinition1).matchedArguments(pickleStep1);
        verify(stepDefinition2).matchedArguments(pickleStep1);

        //check cache
        Glue.CacheEntry entry = glue.matchedStepDefinitionsCache.get(stepText);
        assertEquals(stepDefinition1,entry.stepDefinition);

        PickleStep pickleStep2 = getPickleStep(stepText);
        assertEquals(stepDefinition1, glue.stepDefinitionMatch(featurePath, pickleStep2).getStepDefinition());
        //verify that match wasn't called again
        verify(stepDefinition1).matchedArguments(any(PickleStep.class));
        verify(stepDefinition2).matchedArguments(any(PickleStep.class));

    }

    @Test
    public void throws_ambiguous_steps_def_exception_when_many_patterns_match() {
        StepDefinition stepDefinition1 = getStepDefinitionMockWithPattern("pattern1");
        StepDefinition stepDefinition2 = getStepDefinitionMockWithPattern("^pattern2");
        StepDefinition stepDefinition3 = getStepDefinitionMockWithPattern("^pattern[1,3]");
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        glue.addStepDefinition(stepDefinition3);
        String featurePath = "someFeature.feature";

        checkAmbiguousCalled(featurePath);
        //try again to verify if we don't cache when there is ambiguous step
        checkAmbiguousCalled(featurePath);
    }

    private void checkAmbiguousCalled(String featurePath) {
        boolean ambiguousCalled = false;
        try {

            glue.stepDefinitionMatch(featurePath, getPickleStep("pattern1"));
        } catch (AmbiguousStepDefinitionsException e) {
            assertEquals(2,e.getMatches().size());
            ambiguousCalled = true;
        }
        assertTrue(ambiguousCalled);
    }

    private static PickleStep getPickleStep(String text) {
        return new PickleStep(text, Collections.<Argument>emptyList(), Collections.<PickleLocation>emptyList());
    }

    private static StepDefinition getStepDefinitionMockWithPattern(String pattern) {
        StepExpression expression = new StepExpressionFactory(new TypeRegistry(ENGLISH)).createExpression(pattern);
        final ArgumentMatcher argumentMatcher = new ExpressionArgumentMatcher(expression);
        StepDefinition stepDefinition = mock(StepDefinition.class);
        when(stepDefinition.getPattern()).thenReturn(pattern);
        when(stepDefinition.matchedArguments(any(PickleStep.class))).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) {
                return argumentMatcher.argumentsFrom((PickleStep) invocationOnMock.getArgument(0));
            }
        });
        return stepDefinition;
    }
}
