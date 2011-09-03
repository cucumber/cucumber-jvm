package cucumber.runtime;

import cucumber.resources.Resources;
import cucumber.runtime.converters.LocalizedXStreams;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.util.*;

import static java.util.Arrays.asList;

public class Runtime {
    private final List<Step> undefinedSteps = new ArrayList<Step>();
    private final List<Backend> backends;
    private final LocalizedXStreams localizedXStreams = new LocalizedXStreams();

    public Runtime(Backend... backends) {
        this.backends = asList(backends);
    }

    public Runtime(String packageName) {
        backends = Resources.instantiateSubclasses(Backend.class, "cucumber.runtime", packageName);
    }

    public StepDefinitionMatch stepDefinitionMatch(String uri, Step step) {
        List<StepDefinitionMatch> matches = stepDefinitionMatches(uri, step);
        if (matches.size() == 0) {
            undefinedSteps.add(step);
            return null;
        }
        if (matches.size() == 1) {
            return matches.get(0);
        } else {
            throw new AmbiguousStepDefinitionsException(matches);
        }
    }

    private List<StepDefinitionMatch> stepDefinitionMatches(String uri, Step step) {
        List<StepDefinitionMatch> result = new ArrayList<StepDefinitionMatch>();
        for (Backend backend : backends) {
            for (StepDefinition stepDefinition : backend.getStepDefinitions()) {
                List<Argument> arguments = stepDefinition.matchedArguments(step);
                if (arguments != null) {
                    result.add(new StepDefinitionMatch(arguments, stepDefinition, uri, step, localizedXStreams));
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

    public World newWorld(Set<String> tags) {
        return new World(backends, this, tags);
    }

}
