package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Step;

import java.util.*;

/**
 * Generates metadata to be used for Code Completion: https://github.com/cucumber/gherkin/wiki/Code-Completion
 * 
 * The intention is to run this over all step definitions and all features. This can happen at different times,
 * and we need to decide what's the best:
 * 
 * 1) At the end of a Cucumber run. Pros: Developers will not forget to run it. Cons: It will write "bad" data if Cucumber only runs a subset of features/stepdefs
 * 
 * 2) As a separate process apart from Cucumber. Pros: We can make sure all stpedefs/features are included. Cons: Developers might forget to run it
 * 
 * If we go for a separate process it could be run in the background by IDE plugins...
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
