package cucumber.runtime;

import cucumber.resources.Resources;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Step;

import java.util.*;

import static java.util.Arrays.asList;

public class Runtime {
    private final List<Step> undefinedSteps = new ArrayList<Step>();
    private final List<Backend> backends;
    private final List<String> codePaths;
    private World world;

    public Runtime() {
        this(System.getProperty("cucumber.glue") != null ? asList(System.getProperty("cucumber.glue").split(",")) : new ArrayList<String>());
    }

    public Runtime(List<String> codePaths) {
        backends = Resources.instantiateSubclasses(Backend.class, "cucumber.runtime", new Class[0], new Object[0]);
        this.codePaths = codePaths;
    }

    public void createWorld(List<String> extraCodePaths, Set<String> tags) {
        List<String> allCodePaths = new ArrayList<String>(codePaths);
        allCodePaths.addAll(extraCodePaths);

        world = new World(backends, this, tags);
        world.prepare(allCodePaths);
    }

    public Throwable runStep(String uri, Step step, Reporter reporter, Locale locale) {
        return world.runStep(uri, step, reporter, locale);
    }

    public void disposeWorld() {
        world.dispose();
    }

    /**
     * @return a list of code snippets that the developer can use to implement undefined steps.
     *         This should be displayed after a run.
     */
    public List<String> getSnippets() {
        // TODO: Convert "And" and "But" to the Given/When/Then keyword above in the Gherkin source.
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

    public void undefinedStep(Step step) {
        undefinedSteps.add(step);
    }
}
