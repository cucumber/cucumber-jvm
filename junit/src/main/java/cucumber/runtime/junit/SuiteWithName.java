package cucumber.runtime.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;

class SuiteWithName extends Suite {
    private final String name;

    public static SuiteWithName newSuite(Class<?> testClass, Runtime runtime, CucumberScenarioOutline cucumberScenarioOutline, JUnitReporter jUnitReporter, NameProvider nameProvider) throws InitializationError {
        List<Runner> children = new ArrayList<Runner>();
        for (CucumberExamples cucumberExamples : cucumberScenarioOutline.getCucumberExamplesList()) {
            children.add(newSuite(testClass, runtime, cucumberExamples, jUnitReporter, nameProvider));
        }
        String name = nameProvider.getName(cucumberScenarioOutline);
        return new SuiteWithName(testClass, children, name);
    }

    public static SuiteWithName newSuite(Class<?> testClass, Runtime runtime, CucumberExamples cucumberExamples, JUnitReporter jUnitReporter, NameProvider nameProvider) throws InitializationError {
        List<Runner> children = new ArrayList<Runner>();
        List<CucumberScenario> exampleScenarios = cucumberExamples.createExampleScenarios();
        for (CucumberScenario scenario : exampleScenarios) {
            String name = nameProvider.getName(scenario);                    
            children.add(new ExecutionUnitRunner(testClass, runtime, name, scenario, jUnitReporter));
        }
        String name = nameProvider.getName(cucumberExamples.getExamples());
        return new SuiteWithName(testClass, children, name);
    }

    private SuiteWithName(Class<?> testClass, List<Runner> runners, String name) throws InitializationError {
        super(testClass, runners);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
