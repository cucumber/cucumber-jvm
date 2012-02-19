package cucumber.runtime.autocomplete;

import cucumber.runtime.StepDefinition;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Generates metadata to be used for Code Completion: https://github.com/cucumber/gherkin/wiki/Code-Completion
 */
public class StepdefGenerator {
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

    public List<MetaStepdef> generate(Collection<StepDefinition> stepDefinitions, List<CucumberFeature> features) {
        List<MetaStepdef> result = new ArrayList<MetaStepdef>();

        List<StepDefinition> sortedStepdefs = new ArrayList<StepDefinition>();
        sortedStepdefs.addAll(stepDefinitions);
        Collections.sort(sortedStepdefs, STEP_DEFINITION_COMPARATOR);
        for (StepDefinition stepDefinition : sortedStepdefs) {
            MetaStepdef metaStepdef = new MetaStepdef();
            metaStepdef.source = stepDefinition.getPattern();
            metaStepdef.flags = ""; // TODO = get the flags too
            for (CucumberFeature feature : features) {
                List<CucumberTagStatement> cucumberTagStatements = feature.getFeatureElements();
                for (CucumberTagStatement tagStatement : cucumberTagStatements) {
                    List<Step> steps = tagStatement.getSteps();
                    for (Step step : steps) {
                        List<Argument> arguments = stepDefinition.matchedArguments(step);
                        if (arguments != null) {
                            MetaStepdef.MetaStep ms = new MetaStepdef.MetaStep();
                            ms.name = step.getName();
                            for (Argument argument : arguments) {
                                MetaStepdef.MetaArgument ma = new MetaStepdef.MetaArgument();
                                ma.offset = argument.getOffset();
                                ma.val = argument.getVal();
                                ms.args.add(ma);
                            }
                            metaStepdef.steps.add(ms);
                        }
                    }
                }
                Collections.sort(cucumberTagStatements, CUCUMBER_TAG_STATEMENT_COMPARATOR);
            }
            result.add(metaStepdef);
        }
        return result;
    }

}
