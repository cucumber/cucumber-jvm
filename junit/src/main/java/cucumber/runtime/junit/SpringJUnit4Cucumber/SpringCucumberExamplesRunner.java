package com.txtr.automater.tests.helper.SpringJUnit4Cucumber;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.Runtime;
import cucumber.runtime.junit.ExecutionUnitRunner;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberScenario;

class SpringCucumberExamplesRunner extends Suite{
	 private final CucumberExamples cucumberExamples;
	    private Description description;

	    protected SpringCucumberExamplesRunner(Runtime runtime, CucumberExamples cucumberExamples, JUnitReporter jUnitReporter) throws InitializationError {
	        super(SpringCucumberExamplesRunner.class, new ArrayList<Runner>());
	        this.cucumberExamples = cucumberExamples;

	        List<CucumberScenario> exampleScenarios = cucumberExamples.createExampleScenarios();
	        for (CucumberScenario scenario : exampleScenarios) {
	            try {
	            	//Sprin
	                ExecutionUnitRunner exampleScenarioRunner = new ExecutionUnitRunner(runtime, scenario, jUnitReporter);
	                getChildren().add(exampleScenarioRunner);
	            } catch (InitializationError initializationError) {
	                initializationError.printStackTrace();
	            }
	        }
	    }

	    @Override
	    protected String getName() {
	        return cucumberExamples.getExamples().getKeyword() + ": " + cucumberExamples.getExamples().getName();
	    }

	    @Override
	    public Description getDescription() {
	        if (description == null) {
	            description = Description.createSuiteDescription(getName(), cucumberExamples.getExamples());
	            for (Runner child : getChildren()) {
	                description.addChild(describeChild(child));
	            }
	        }
	        return description;
	    }
}
