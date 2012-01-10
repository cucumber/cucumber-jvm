package cucumber.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cucumber.resources.Consumer;
import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.runtime.autocomplete.MetaStepdef;
import cucumber.runtime.autocomplete.StepdefGenerator;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Step;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * This is the main entry point for running Cucumber features.
 */
public class Runtime {
    private static final byte ERRORS = 0x1;
    private static final List<Object> NO_FILTERS = emptyList();
    private static final Collection<String> NO_TAGS = emptyList();

    private final UndefinedStepsTracker tracker;
    private final List<Throwable> errors = new ArrayList<Throwable>();
    private final List<? extends Backend> backends;
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

    public Runtime(List<String> gluePaths, List<? extends Backend> backends, boolean isDryRun) {
        this.backends = backends;
        this.gluePaths = gluePaths;
        this.isDryRun = isDryRun;
        this.tracker = new UndefinedStepsTracker(backends);
    }

    public void addError(Throwable error) {
        errors.add(error);
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

    public void buildBackendWorlds(List<String> gluePaths, World world) {
        for (Backend backend : backends) {
            backend.buildWorld(gluePaths, world);
        }
        tracker.reset();
    }

    public void disposeBackendWorlds() {
        for (Backend backend : backends) {
            backend.disposeWorld();
        }
    }

    public void writeStepdefsJson(List<String> filesOrDirs, File dotCucumber) throws IOException {
        List<CucumberFeature> features = load(filesOrDirs, NO_FILTERS);
        World world = new World(this, NO_TAGS);
        buildBackendWorlds(gluePaths, world);
        List<StepDefinition> stepDefs = world.getStepDefinitions();
        List<MetaStepdef> metaStepdefs = new StepdefGenerator().generate(stepDefs, features);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(metaStepdefs);

        FileWriter metaJson = new FileWriter(new File(dotCucumber, "stepdefs.json"));
        metaJson.append(json);
        metaJson.close();
    }

    public boolean isDryRun() {
        return isDryRun;
    }

    public List<Throwable> getErrors() {
        return errors;
    }

    public byte exitStatus() {
        byte result = 0x0;
        if (!errors.isEmpty()) {
            result |= ERRORS;
        }
        return result;
    }

    public void storeStepKeyword(Step step, Locale locale) {
        tracker.storeStepKeyword(step, locale);
    }

    public void addUndefinedStep(Step step, Locale locale) {
        tracker.addUndefinedStep(step, locale);
    }

    public List<String> getSnippets() {
        return tracker.getSnippets();
    }
}
