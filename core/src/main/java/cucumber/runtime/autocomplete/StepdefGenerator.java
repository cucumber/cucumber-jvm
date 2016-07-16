package cucumber.runtime.autocomplete;

import cucumber.runtime.Argument;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.model.CucumberFeature;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleStep;

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

    public List<MetaStepdef> generate(Collection<StepDefinition> stepDefinitions, List<CucumberFeature> features) {
        List<MetaStepdef> result = new ArrayList<MetaStepdef>();

        List<StepDefinition> sortedStepdefs = new ArrayList<StepDefinition>();
        sortedStepdefs.addAll(stepDefinitions);
        Collections.sort(sortedStepdefs, STEP_DEFINITION_COMPARATOR);
        Compiler compiler = new Compiler();
        for (StepDefinition stepDefinition : sortedStepdefs) {
            MetaStepdef metaStepdef = new MetaStepdef();
            metaStepdef.source = stepDefinition.getPattern();
            metaStepdef.flags = ""; // TODO = get the flags too
            for (CucumberFeature feature : features) {
                for (Pickle pickle : compiler.compile(feature.getGherkinFeature(), feature.getPath())) {
                    for (PickleStep step : pickle.getSteps()) {
                        List<Argument> arguments = stepDefinition.matchedArguments(step);
                        if (arguments != null) {
                            MetaStepdef.MetaStep ms = new MetaStepdef.MetaStep();
                            ms.name = step.getText();
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
            }
            result.add(metaStepdef);
        }
        return result;
    }

}
