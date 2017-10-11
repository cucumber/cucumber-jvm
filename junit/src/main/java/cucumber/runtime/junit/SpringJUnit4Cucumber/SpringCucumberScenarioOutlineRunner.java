package com.txtr.automater.tests.helper.SpringJUnit4Cucumber;

import java.util.ArrayList;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.Runtime;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberScenarioOutline;
import com.txtr.automater.tests.helper.SpringJUnit4Cucumber.SpringCucumberExamplesRunner;

class SpringCucumberScenarioOutlineRunner extends Suite{
	private final CucumberScenarioOutline cucumberScenarioOutline;
    private Description description;

    public SpringCucumberScenarioOutlineRunner(Runtime runtime, CucumberScenarioOutline cucumberScenarioOutline, JUnitReporter jUnitReporter) throws InitializationError {
        super(null, new ArrayList<Runner>());
        this.cucumberScenarioOutline = cucumberScenarioOutline;
        for (CucumberExamples cucumberExamples : cucumberScenarioOutline.getCucumberExamplesList()) {
            getChildren().add(new SpringCucumberExamplesRunner(runtime, cucumberExamples, jUnitReporter));
        }
    }

    @Override
    public String getName() {
        return cucumberScenarioOutline.getVisualName();
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), cucumberScenarioOutline.getGherkinModel());
            for (Runner child : getChildren()) {
                description.addChild(describeChild(child));
            }
        }
        return description;
    }
}