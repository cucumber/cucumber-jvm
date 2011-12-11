package cucumber.runtime;

import gherkin.I18n;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

public class UndefinedStepsTracker {
    private final List<Step> undefinedSteps = new ArrayList<Step>();
    private final Iterable<? extends Backend> backends;

    private String lastGivenWhenThenStepKeyword;

    public UndefinedStepsTracker(Iterable<? extends Backend> backends) {
        this.backends = backends;
    }

    public void reset() {
        lastGivenWhenThenStepKeyword = null;
    }
    
    /**
     * @return a list of code snippets that the developer can use to implement undefined steps.
     *         This should be displayed after a run.
     */
    public List<String> getSnippets() {
        // TODO: Convert "And" and "But" to the Given/When/Then keyword above in the Gherkin source.
        List<String> snippets = new ArrayList<String>();
        for (Step step : undefinedSteps) {
            for (Backend backend : backends) {
                String snippet = backend.getSnippet(step);
                if (!snippets.contains(snippet)) {
                    snippets.add(snippet);
                }
            }
        }
        return snippets;
    }

    public void storeStepKeyword(Step step, Locale locale) {
        String keyword = step.getKeyword();
        if (isGivenWhenThenKeyword(keyword, locale)) {
            lastGivenWhenThenStepKeyword = keyword;
        }
        if (lastGivenWhenThenStepKeyword == null) {
            lastGivenWhenThenStepKeyword = keyword;
        }
    }

    public void addUndefinedStep(Step step, Locale locale) {
        undefinedSteps.add(givenWhenThenStep(step, locale));
    }

    private boolean isGivenWhenThenKeyword(String keyword, Locale locale) {
        I18n i18n = new I18n("en");
        for (String gwts : asList("given", "when", "then")) {
            List<String> keywords = i18n.keywords(gwts);
            if (keywords.contains(keyword) && !"* ".equals(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Step givenWhenThenStep(Step step, Locale locale) {
        if (isGivenWhenThenKeyword(step.getKeyword(), locale)) {
            return step;
        } else {
            if(lastGivenWhenThenStepKeyword == null) {
                I18n i18n = new I18n("en");
                List<String> givenKeywords = new ArrayList<String>(i18n.keywords("given"));
                givenKeywords.remove("* ");
                lastGivenWhenThenStepKeyword = givenKeywords.get(0);
            }
            return new Step(step.getComments(), lastGivenWhenThenStepKeyword, step.getName(), step.getLine(), step.getRows(), step.getDocString());
        }
    }
}
