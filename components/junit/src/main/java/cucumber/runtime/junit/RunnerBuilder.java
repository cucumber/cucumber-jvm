package cucumber.runtime.junit;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import gherkin.model.*;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

public class RunnerBuilder implements Visitor {
    private final Runtime runtime;
    private List<Step> steps = null;
    private List<ParentRunner> exampleScenarioRunners = null;
    private List<ParentRunner> examplesRunners = null;
    private List<ParentRunner> featureElementRunners = null;

    public RunnerBuilder(Runtime runtime) {
        this.runtime = runtime;
    }

    public void visitFeature(Feature feature) {
    }

    public void visitBackground(Background background) {
    }

    public void visitScenario(Scenario scenario) {
        try {
            if (featureElementRunners == null) {
                featureElementRunners = new ArrayList<ParentRunner>();
            }
            featureElementRunners.add(new ScenarioRunner(runtime, getName(scenario), steps));
            steps = null;
        } catch (InitializationError e) {
            throw new CucumberException("Should never happen", e);
        }
    }

    public void visitScenarioOutline(ScenarioOutline scenarioOutline) {
        try {
            featureElementRunners.add(new RunnerWithChildren(getName(scenarioOutline), examplesRunners));
            steps = null;
        } catch (InitializationError e) {
            throw new CucumberException("Should never happen", e);
        }
    }

    public void visitStep(Step step) {
        if (steps == null) {
            steps = new ArrayList<Step>();
        }
        steps.add(step);
    }

    public void visitExamples(Examples examples) {
        try {
            if (examplesRunners == null) {
                examplesRunners = new ArrayList<ParentRunner>();
            }
            examplesRunners.add(new RunnerWithChildren(getName(examples), exampleScenarioRunners));
            exampleScenarioRunners = null;
        } catch (InitializationError e) {
            throw new CucumberException("Should never happen", e);
        }
    }

    public void visitExampleScenario(ExampleScenario exampleScenario) {
        try {
            if (exampleScenarioRunners == null) {
                exampleScenarioRunners = new ArrayList<ParentRunner>();
            }
            exampleScenarioRunners.add(new ScenarioRunner(runtime, getName(exampleScenario), steps));
            steps = null;
        } catch (InitializationError e) {
            throw new CucumberException("Should never happen", e);
        }
    }

    public List<ParentRunner> getFeatureElementRunners() {
        return featureElementRunners;
    }

    private String getName(DescribedStatement statement) {
        return statement.getKeyword() + ": " + statement.getName();
    }

}
