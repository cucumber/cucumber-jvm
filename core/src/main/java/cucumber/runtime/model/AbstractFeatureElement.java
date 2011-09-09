package cucumber.runtime.model;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Step;

import java.util.List;
import java.util.Set;

import cucumber.runtime.Runtime;
import cucumber.runtime.World;

public abstract class AbstractFeatureElement extends AbstractStepContainer implements FeatureElement {
    private World world;

    public AbstractFeatureElement(CucumberFeature cucumberFeature, String uri) {
        super(cucumberFeature, uri);
    }

    @Override
    public void run(Runtime runtime, Formatter formatter, Reporter reporter) {
        prepare(runtime);
        List<Step> stepsToRun = getSteps();
        if (getCucumberFeature().getBackground() != null) {
            stepsToRun.addAll(0, getCucumberFeature().getBackground().getSteps());
        }
        run(world, formatter, reporter, stepsToRun);
        dispose();
    }

    public void prepare(Runtime runtime) {
        world = runtime.newWorld(tags());
        world.prepare();
    }

    public void dispose() {
        world.dispose();
    }

    public World getWorld() {
        return world;
    }

    public abstract void run(World world, Formatter formatter, Reporter reporter, List<Step> stepsToRun);

    public abstract Set<String> tags();}
