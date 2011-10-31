package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.model.Step;

import java.util.*;

/**
 * Generates metadata to be used for Code Completion: https://github.com/cucumber/gherkin/wiki/Code-Completion
 */
public class Metadata {
    private static final Comparator<StepDefinition> STEP_DEFINITION_COMPARATOR = new Comparator<StepDefinition>() {
        @Override
        public int compare(StepDefinition a, StepDefinition b) {
            return a.getPattern().compareTo(b.getPattern());
        }
    };

    private static final Comparator<CucumberTagStatement> CUCUMBER_TAG_STATEMENT_COMPARATOR = new Comparator<CucumberTagStatement>() {
        @Override
        public int compare(CucumberTagStatement a, CucumberTagStatement b) {
            return a.getVisualName().compareTo(b.getVisualName());
        }
    };

    public Map<String, List<String>> generate(List<StepDefinition> stepDefs, List<CucumberFeature> features) {
        Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();

        Collections.sort(stepDefs, STEP_DEFINITION_COMPARATOR);

        for (StepDefinition stepDef : stepDefs) {
            Set<String> matchingSteps = new HashSet<String>();
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
                Collections.sort(cucumberTagStatements, CUCUMBER_TAG_STATEMENT_COMPARATOR);
            }
            List<String> matchingStepsList = new ArrayList<String>(matchingSteps);
            Collections.sort(matchingStepsList);
            result.put(stepDef.getPattern(), matchingStepsList);
        }
        return result;
    }

}
