package cucumber.runtime;

import cucumber.resources.Consumer;
import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.runtime.model.CucumberBackground;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Step;

import java.util.*;

import static java.util.Arrays.asList;

/**
 * This is the main entry point for running Cucumber features.
 */
public class Runtime {
    private final List<Step> undefinedSteps = new ArrayList<Step>();
    private final List<String> codePaths;
    private final List<Backend> backends;
    private World world;

    public Runtime() {
        this(System.getProperty("cucumber.glue") != null ? asList(System.getProperty("cucumber.glue").split(",")) : new ArrayList<String>());
    }

    public Runtime(List<String> codePaths) {
        this(codePaths, Resources.instantiateSubclasses(Backend.class, "cucumber.runtime", new Class[0], new Object[0]));
    }

    public Runtime(List<String> codePaths, List<Backend> backends) {
        this.backends = backends;
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

    public void run(List<String> filesOrDirs, final List<Object> filters, gherkin.formatter.Formatter formatter, Reporter reporter) {
        for (CucumberFeature cucumberFeature : load(filesOrDirs, filters)) {
            run(cucumberFeature, formatter, reporter);
        }
    }

    public void run(CucumberFeature cucumberFeature, Formatter formatter, Reporter reporter) {
        formatter.uri(cucumberFeature.getUri());
        formatter.feature(cucumberFeature.getFeature());
        for (CucumberScenario cucumberScenario : cucumberFeature.getCucumberScenarios()) {
            run(cucumberScenario, formatter, reporter);
        }
    }

    public void run(CucumberScenario cucumberScenario, Formatter formatter, Reporter reporter) {
        // TODO: Maybe get extraPaths from scenario

        // TODO: split up prepareAndFormat so we can run Background in isolation.
        // Or maybe just try to make Background behave like a regular Scenario?? Printing wise at least.

        createWorld(new ArrayList<String>(), cucumberScenario.tags());

        CucumberBackground cucumberBackground = cucumberScenario.getCucumberBackground();
        if (cucumberBackground != null) {
            runBackground(cucumberBackground, formatter, reporter);
        }

        cucumberScenario.format(formatter);
        for (Step step : cucumberScenario.getSteps()) {
            runStep(cucumberScenario.getUri(), step, reporter, cucumberScenario.getLocale());
        }
        disposeWorld();
    }

    public Throwable runBackground(CucumberBackground cucumberBackground, Formatter formatter, Reporter reporter) {
        cucumberBackground.format(formatter);
        List<Step> steps = cucumberBackground.getSteps();
        Throwable failure = null;
        for (Step step : steps) {
            Throwable e = runStep(cucumberBackground.getUri(), step, reporter, cucumberBackground.getLocale());
            if (e != null) {
                failure = e;
            }
        }
        return failure;
    }
    
    private List<CucumberFeature> load(List<String> filesOrDirs, final List<Object> filters) {
        final List<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
        final FeatureBuilder builder = new FeatureBuilder(cucumberFeatures);
        for (String fileOrDir : filesOrDirs) {
            Resources.scan(fileOrDir, ".feature", new Consumer() {
                @Override
                public void consume(Resource resource) {
                    builder.parse(resource, filters);
                }
            });
        }
        return cucumberFeatures;
    }
}
