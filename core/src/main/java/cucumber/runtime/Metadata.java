package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Step;

import java.util.*;

/**
 * Generates metadata to be used for Code Completion: https://github.com/cucumber/gherkin/wiki/Code-Completion
 */
public class Metadata {
    public Map<String, List<String>> generate(List<StepDefinition> stepDefs, List<CucumberFeature> features) {
        Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();

        for (StepDefinition stepDef : stepDefs) {
            List<String> matchingSteps = new ArrayList<String>();
            result.put(stepDef.getPattern(), matchingSteps);
            for (CucumberFeature feature : features) {
                List<CucumberScenario> cucumberScenarios = feature.getCucumberScenarios();
                for (CucumberScenario scenario : cucumberScenarios) {
                    List<Step> steps = scenario.getSteps();
                    for (Step step : steps) {
                        if(stepDef.matchedArguments(step) != null) {
                            matchingSteps.add(step.getName());
                        }
                    }
                }

            }
        }
        return result;
    }
}
