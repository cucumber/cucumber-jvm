package cucumber.runtime;

import cucumber.classpath.Classpath;
import cucumber.runtime.transformers.Transformer;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;

public class Runtime {
    private final List<Backend> backends;
    private final List<Step> undefinedSteps = new ArrayList<Step>();
    private Transformer transformer;

    public Runtime(Backend... backends) {
        this.backends = asList(backends);
    }

    public Runtime(String packageName) {
        backends = Classpath.instantiateSubclasses(Backend.class, "cucumber.runtime", packageName);
    }

    public StepDefinitionMatch stepDefinitionMatch(Step step) {
        List<StepDefinitionMatch> matches = stepDefinitionMatches(step);
        if (matches.size() == 0) {
            undefinedSteps.add(step);
            return null;
        }
        if (matches.size() == 1) {
            return matches.get(0);
        } else {
            // TODO: Ambiguous for > 1
            throw new RuntimeException("TODO: Support ambiguous matches");
        }
    }

    private List<StepDefinitionMatch> stepDefinitionMatches(Step step) {
        List<StepDefinitionMatch> result = new ArrayList<StepDefinitionMatch>();
        for (Backend backend : backends) {
            for (StepDefinition stepDefinition : backend.getStepDefinitions()) {
                List<Argument> arguments = stepDefinition.matchedArguments(step);
                if (arguments != null) {
                    result.add(new StepDefinitionMatch(arguments, stepDefinition, step, getTransformer()));
                }
            }
        }
        return result;
    }

    /**
     * @return a list of code snippets that the developer can use to implement undefined steps.
     *         This should be displayed after a run.
     */
    public List<String> getSnippets() {
        // TODO: Convert "And" and "But" to the Given/When/Then keyword above.
        Collections.sort(undefinedSteps, new Comparator<Step>() {
            public int compare(Step a, Step b) {
                int keyword = a.getKeyword().compareTo(b.getKeyword());
                if (keyword == 0) {
                    return a.getName().compareTo(b.getName());
                } else {
                    return keyword;
                }
            }
        });

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

    public World newWorld() {
        return new World(backends, this);
    }

    public Transformer getTransformer() {
        if (this.transformer == null) {
            this.transformer = new Transformer();
        }
        return this.transformer;
    }

    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }
}
