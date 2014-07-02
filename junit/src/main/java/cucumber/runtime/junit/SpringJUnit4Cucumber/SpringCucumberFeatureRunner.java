package com.txtr.automater.tests.helper.SpringJUnit4Cucumber;

import gherkin.formatter.model.Feature;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.junit.ExecutionUnitRunner;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;

public class SpringCucumberFeatureRunner extends ParentRunner<ParentRunner>{
	 private final List<ParentRunner> children = new ArrayList<ParentRunner>();

	    private final CucumberFeature cucumberFeature;
	    private final Runtime runtime;
	    private final JUnitReporter jUnitReporter;
	    private Description description;

	    public SpringCucumberFeatureRunner(CucumberFeature cucumberFeature, Runtime runtime, JUnitReporter jUnitReporter) throws InitializationError {
	        super(null);
	        this.cucumberFeature = cucumberFeature;
	        this.runtime = runtime;
	        this.jUnitReporter = jUnitReporter;
	        buildFeatureElementRunners();
	    }

	    @Override
	    public String getName() {
	        Feature feature = cucumberFeature.getGherkinFeature();
	        return feature.getKeyword() + ": " + feature.getName();
	    }

	    @Override
	    public Description getDescription() {
	        if (description == null) {
	            description = Description.createSuiteDescription(getName(), cucumberFeature.getGherkinFeature());
	            for (ParentRunner child : getChildren()) {
	                description.addChild(describeChild(child));
	            }
	        }
	        return description;
	    }

	    @Override
	    protected List<ParentRunner> getChildren() {
	        return children;
	    }

	    @Override
	    protected Description describeChild(ParentRunner child) {
	        return child.getDescription();
	    }

	    @Override
	    protected void runChild(ParentRunner child, RunNotifier notifier) {
	        child.run(notifier);
	    }

	    @Override
	    public void run(RunNotifier notifier) {
	        jUnitReporter.uri(cucumberFeature.getPath());
	        jUnitReporter.feature(cucumberFeature.getGherkinFeature());
	        super.run(notifier);
	        jUnitReporter.eof();
	    }

	    private void buildFeatureElementRunners() {
	        for (CucumberTagStatement cucumberTagStatement : cucumberFeature.getFeatureElements()) {
	            try {
	                ParentRunner featureElementRunner;
	                if (cucumberTagStatement instanceof CucumberScenario) {
	                    featureElementRunner = new ExecutionUnitRunner(runtime, (CucumberScenario) cucumberTagStatement, jUnitReporter);
	                } else {
	                    featureElementRunner = new SpringCucumberScenarioOutlineRunner(runtime, (CucumberScenarioOutline) cucumberTagStatement, jUnitReporter);
	                }
	                children.add(featureElementRunner);
	            } catch (InitializationError e) {
	                throw new CucumberException("Failed to create scenario runner", e);
	            }
	        }
	    }

}
	
	