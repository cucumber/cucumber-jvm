package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                List<CucumberTagStatement> cucumberTagStatements = feature.getFeatureElements();
                for (CucumberTagStatement tagStatement : cucumberTagStatements) {
                    List<Step> steps = tagStatement.getSteps();
                    for (Step step : steps) {
                        if (stepDef.matchedArguments(step) != null) {
                            matchingSteps.add(step.getName());
                        }
                    }
                }

            }
        }
        return result;
    }
}
