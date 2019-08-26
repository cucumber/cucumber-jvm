package cucumber.runtime;

import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.Snippet;
import cucumber.runtime.snippets.SnippetGenerator;
import cucumber.runtime.snippets.UnderscoreConcatenator;
import gherkin.I18n;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UndefinedStepsTrackerTest {

    private static final I18n ENGLISH = new I18n("en");
    private FunctionNameGenerator functionNameGenerator = new FunctionNameGenerator(new UnderscoreConcatenator());

    @Test
    public void has_undefined_steps() {
        UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();
        undefinedStepsTracker.addUndefinedStep(new Step(null, "Given ", "A", 1, null, null), ENGLISH);
        assertTrue(undefinedStepsTracker.hasUndefinedSteps());
    }

    @Test
    public void has_no_undefined_steps() {
        UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();
        assertFalse(undefinedStepsTracker.hasUndefinedSteps());
    }

    @Test
    public void removes_duplicates() {
        Backend backend = new TestBackend();
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.storeStepKeyword(new Step(null, "Given ", "A", 1, null, null), ENGLISH);
        tracker.addUndefinedStep(new Step(null, "Given ", "B", 1, null, null), ENGLISH);
        tracker.addUndefinedStep(new Step(null, "Given ", "B", 1, null, null), ENGLISH);
        assertEquals("[Given ^B$]", tracker.getSnippets(asList(backend), functionNameGenerator).toString());
    }

    @Test
    public void converts_and_to_previous_step_keyword() {
        Backend backend = new TestBackend();
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.storeStepKeyword(new Step(null, "When ", "A", 1, null, null), ENGLISH);
        tracker.storeStepKeyword(new Step(null, "And ", "B", 1, null, null), ENGLISH);
        tracker.addUndefinedStep(new Step(null, "But ", "C", 1, null, null), ENGLISH);
        assertEquals("[When ^C$]", tracker.getSnippets(asList(backend), functionNameGenerator).toString());
    }

    @Test
    public void doesnt_try_to_use_star_keyword() {
        Backend backend = new TestBackend();
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.storeStepKeyword(new Step(null, "When ", "A", 1, null, null), ENGLISH);
        tracker.storeStepKeyword(new Step(null, "And ", "B", 1, null, null), ENGLISH);
        tracker.addUndefinedStep(new Step(null, "* ", "C", 1, null, null), ENGLISH);
        assertEquals("[When ^C$]", tracker.getSnippets(asList(backend), functionNameGenerator).toString());
    }

    @Test
    public void star_keyword_becomes_given_when_no_previous_step() {
        Backend backend = new TestBackend();
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.addUndefinedStep(new Step(null, "* ", "A", 1, null, null), ENGLISH);
        assertEquals("[Given ^A$]", tracker.getSnippets(asList(backend), functionNameGenerator).toString());
    }

    @Test
    public void snippets_are_generated_for_correct_locale() throws Exception {
        Backend backend = new TestBackend();
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.addUndefinedStep(new Step(null, "Если ", "Б", 1, null, null), new I18n("ru"));
        assertEquals("[Если ^Б$]", tracker.getSnippets(asList(backend), functionNameGenerator).toString());
    }

    private class TestBackend implements Backend {
        @Override
        public void loadGlue(Glue glue, List<String> gluePaths) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void buildWorld() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void disposeWorld() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getSnippet(Step step, FunctionNameGenerator functionNameGenerator) {
            return new SnippetGenerator(new TestSnippet()).getSnippet(step, functionNameGenerator);
        }
    }

    private class TestSnippet implements Snippet {
        @Override
        public String template() {
            return "{0} {1}";
        }

        @Override
        public String tableHint() {
            return null;
        }

        @Override
        public String arguments(List<Class<?>> argumentTypes) {
            return argumentTypes.toString();
        }

        @Override
        public String namedGroupStart() {
            return null;
        }

        @Override
        public String namedGroupEnd() {
            return null;
        }

        @Override
        public String escapePattern(String pattern) {
            return pattern;
        }
    }
}
