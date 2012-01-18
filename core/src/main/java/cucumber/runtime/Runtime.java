package cucumber.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cucumber.io.ClasspathResourceLoader;
import cucumber.io.ResourceLoader;
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

import static cucumber.runtime.model.CucumberFeature.load;
import static java.util.Collections.emptyList;

/**
 * This is the main entry point for running Cucumber features.
 */
public class Runtime {
    private static final byte ERRORS = 0x1;
    private static final List<Object> NO_FILTERS = emptyList();

    private final UndefinedStepsTracker tracker;
    private final List<Throwable> errors = new ArrayList<Throwable>();
    private final Collection<? extends Backend> backends;
    private final boolean isDryRun;
    private final ResourceLoader resourceLoader;

    private Glue glue;

    public Runtime(List<String> gluePaths, ResourceLoader resourceLoader) {
        this(gluePaths, resourceLoader, false);
    }

    public Runtime(List<String> gluePaths, ResourceLoader resourceLoader, boolean isDryRun) {
        this(gluePaths, resourceLoader, loadBackends(resourceLoader), isDryRun);
    }

    public Runtime(List<String> gluePaths, ResourceLoader resourceLoader, Collection<? extends Backend> backends, boolean isDryRun) {
        this.backends = backends;
        this.resourceLoader = resourceLoader;
        this.isDryRun = isDryRun;
        this.tracker = new UndefinedStepsTracker(backends);

        this.glue = new RuntimeGlue(this);
        for (Backend backend : backends) {
            backend.loadGlue(glue, gluePaths);
        }
    }

    private static Collection<? extends Backend> loadBackends(ResourceLoader resourceLoader) {
        return new ClasspathResourceLoader().instantiateSubclasses(Backend.class, "cucumber/runtime", new Class[]{ResourceLoader.class}, new Object[]{resourceLoader});
    }

    public void addError(Throwable error) {
        errors.add(error);
    }

    /**
     * This is where the first entry happens.
     * Glue shouldn't be passed along, since it's Glue, we should somehow expose the right bits to the various stages
     * so that the appropriate calls can be made at the appropriate time.
     * @param featurePaths
     * @param filters
     * @param formatter
     * @param reporter
     */
    public void run(List<String> featurePaths, final List<Object> filters, gherkin.formatter.Formatter formatter, Reporter reporter) {
        for (CucumberFeature cucumberFeature : load(resourceLoader, featurePaths, filters)) {
            run(cucumberFeature, formatter, reporter);
        }
    }

    /**
     * Runs an individual feature, not all the features
     * @param cucumberFeature
     * @param formatter
     * @param reporter
     */
    public void run(CucumberFeature cucumberFeature, Formatter formatter, Reporter reporter) {
        formatter.uri(cucumberFeature.getUri());
        formatter.feature(cucumberFeature.getFeature());
        for (CucumberTagStatement cucumberTagStatement : cucumberFeature.getFeatureElements()) {
            //Executes an individual scenario?
            cucumberTagStatement.run(formatter, reporter, glue);
        }
        formatter.eof();
    }

    public void buildBackendWorlds() {
        for (Backend backend : backends) {
            backend.buildWorld();
        }
        tracker.reset();
    }

    public void disposeBackendWorlds() {
        for (Backend backend : backends) {
            backend.disposeWorld();
        }
    }

    /**
     * This is the second entry to running features
     *
     * @param featurePaths
     * @param dotCucumber
     * @throws IOException
     */
    public void writeStepdefsJson(List<String> featurePaths, File dotCucumber) throws IOException {
        List<CucumberFeature> features = load(resourceLoader, featurePaths, NO_FILTERS);
        buildBackendWorlds();
        List<StepDefinition> stepDefs = glue.getStepDefinitions();
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

    public Glue getGlue() {
        return glue;
    }
}
