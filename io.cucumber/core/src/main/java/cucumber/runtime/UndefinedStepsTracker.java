package cucumber.runtime;

import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.I18n;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class UndefinedStepsTracker {
    private final List<Step> undefinedSteps = new ArrayList<Step>();

    private String lastGivenWhenThenStepKeyword;

    public void reset() {
        lastGivenWhenThenStepKeyword = null;
    }

    /**
     * @param backends              what backends we want snippets for
     * @param functionNameGenerator responsible for generating method name
     * @return a list of code snippets that the developer can use to implement undefined steps.
     *         This should be displayed after a run.
     */
    public List<String> getSnippets(Iterable<? extends Backend> backends, FunctionNameGenerator functionNameGenerator) {
        // TODO: Convert "And" and "But" to the Given/When/Then keyword above in the Gherkin source.
        List<String> snippets = new ArrayList<String>();
        for (Step step : undefinedSteps) {
            for (Backend backend : backends) {
                String snippet = backend.getSnippet(step, functionNameGenerator);
                if (snippet == null) {
                    throw new NullPointerException("null snippet");
                }
                if (!snippets.contains(snippet)) {
                    snippets.add(snippet);
                }
            }
        }
        return snippets;
    }

    public void storeStepKeyword(Step step, I18n i18n) {
        String keyword = step.getKeyword();
        if (isGivenWhenThenKeyword(keyword, i18n)) {
            lastGivenWhenThenStepKeyword = keyword;
        }
        if (lastGivenWhenThenStepKeyword == null) {
            lastGivenWhenThenStepKeyword = keyword;
        }
    }

    public void addUndefinedStep(Step step, I18n i18n) {
        undefinedSteps.add(givenWhenThenStep(step, i18n));
    }

    private boolean isGivenWhenThenKeyword(String keyword, I18n i18n) {
        for (String gwts : asList("given", "when", "then")) {
            List<String> keywords = i18n.keywords(gwts);
            if (keywords.contains(keyword) && !"* ".equals(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Step givenWhenThenStep(Step step, I18n i18n) {
        if (isGivenWhenThenKeyword(step.getKeyword(), i18n)) {
            return step;
        } else {
            if (lastGivenWhenThenStepKeyword == null) {
                List<String> givenKeywords = new ArrayList<String>(i18n.keywords("given"));
                givenKeywords.remove("* ");
                lastGivenWhenThenStepKeyword = givenKeywords.get(0);
            }
            return new Step(step.getComments(), lastGivenWhenThenStepKeyword, step.getName(), step.getLine(), step.getRows(), step.getDocString());
        }
    }

    public boolean hasUndefinedSteps() {
        return !undefinedSteps.isEmpty();
    }
}
