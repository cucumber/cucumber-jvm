package cucumber.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cucumber.resources.Consumer;
import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Step;

import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * This is the main entry point for running Cucumber features.
 */
public class Runtime {
    private static final List<Object> NO_FILTERS = emptyList();
    private static final Collection<String> NO_TAGS = emptyList();

    private final List<Step> undefinedSteps = new ArrayList<Step>();
    private final List<Backend> backends;
    private final List<String> gluePaths;
    private final boolean isDryRun;


    public Runtime() {
        this(false);
    }

    public Runtime(boolean isDryRun) {
        this(System.getProperty("cucumber.glue") != null ? asList(System.getProperty("cucumber.glue").split(",")) : new ArrayList<String>(), isDryRun);
    }

    public Runtime(List<String> gluePaths, boolean isDryRun) {
        this(gluePaths, Resources.instantiateSubclasses(Backend.class, "cucumber.runtime", new Class[0], new Object[0]), isDryRun);
    }

    public Runtime(List<String> gluePaths, List<Backend> backends, boolean isDryRun) {
        this.backends = backends;
        this.gluePaths = gluePaths;
        this.isDryRun = isDryRun;
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
        for (CucumberTagStatement cucumberTagStatement : cucumberFeature.getFeatureElements()) {
            cucumberTagStatement.run(formatter, reporter, this, backends, gluePaths);
        }
        formatter.eof();
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

    public void buildWorlds(List<String> gluePaths, World world) {
        for (Backend backend : backends) {
            backend.buildWorld(gluePaths, world);
        }
    }

    public void disposeWorlds() {
        for (Backend backend : backends) {
            backend.disposeWorld();
        }
    }

    public void writeMeta(List<String> filesOrDirs, Appendable out) throws IOException {
        List<CucumberFeature> features = load(filesOrDirs, NO_FILTERS);
        World world = new World(this, NO_TAGS);
        buildWorlds(gluePaths, world);
        List<StepDefinition> stepDefs = world.getStepDefinitions();
        Map<String, List<String>> meta = new Metadata().generate(stepDefs, features);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        out.append(gson.toJson(meta));
    }

    public boolean isDryRun() {
        return isDryRun;
    }
}
