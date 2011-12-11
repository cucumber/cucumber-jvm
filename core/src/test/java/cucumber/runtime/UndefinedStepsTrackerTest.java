package cucumber.runtime;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;

public class UndefinedStepsTrackerTest {
    @Test
    public void removes_duplicates() {
        Backend backend = new TestBackend();
        UndefinedStepsTracker tracker = new UndefinedStepsTracker(asList(backend));
        tracker.storeStepKeyword(new Step(null, "Given ", "A", 1, null, null), Locale.ENGLISH);
        tracker.addUndefinedStep(new Step(null, "Given ", "B", 1, null, null), Locale.ENGLISH);
        tracker.addUndefinedStep(new Step(null, "Given ", "B", 1, null, null), Locale.ENGLISH);
        assertEquals("[Given ^B$]", tracker.getSnippets().toString());
    }

    @Test
    public void converts_and_to_previous_step_keyword() {
        Backend backend = new TestBackend();
        UndefinedStepsTracker tracker = new UndefinedStepsTracker(asList(backend));
        tracker.storeStepKeyword(new Step(null, "When ", "A", 1, null, null), Locale.ENGLISH);
        tracker.storeStepKeyword(new Step(null, "And ", "B", 1, null, null), Locale.ENGLISH);
        tracker.addUndefinedStep(new Step(null, "But ", "C", 1, null, null), Locale.ENGLISH);
        assertEquals("[When ^C$]", tracker.getSnippets().toString());
    }

    @Test
    public void doesnt_try_to_use_star_keyword() {
        Backend backend = new TestBackend();
        UndefinedStepsTracker tracker = new UndefinedStepsTracker(asList(backend));
        tracker.storeStepKeyword(new Step(null, "When ", "A", 1, null, null), Locale.ENGLISH);
        tracker.storeStepKeyword(new Step(null, "And ", "B", 1, null, null), Locale.ENGLISH);
        tracker.addUndefinedStep(new Step(null, "* ", "C", 1, null, null), Locale.ENGLISH);
        assertEquals("[When ^C$]", tracker.getSnippets().toString());
    }

    @Test
    public void star_keyword_becomes_given_when_no_previous_step() {
        Backend backend = new TestBackend();
        UndefinedStepsTracker tracker = new UndefinedStepsTracker(asList(backend));
        tracker.addUndefinedStep(new Step(null, "* ", "A", 1, null, null), Locale.ENGLISH);
        assertEquals("[Given ^A$]", tracker.getSnippets().toString());
    }

    private class TestBackend implements Backend {
        @Override
        public void buildWorld(List<String> gluePaths, World world) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void disposeWorld() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getSnippet(Step step) {
            return new TestSnippetGenerator(step).getSnippet();
        }
    }

    private class TestSnippetGenerator extends SnippetGenerator {
        protected TestSnippetGenerator(Step step) {
            super(step);
        }

        @Override
        protected String template() {
            return "{0} {1}";
        }

        @Override
        protected String arguments(List<Class<?>> argumentTypes) {
            return argumentTypes.toString();
        }
    }
}
