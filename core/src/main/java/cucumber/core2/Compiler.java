package cucumber.core2;

import cucumber.runtime.Argument;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.I18n;
import pickles.Pickle;
import pickles.PickleStep;

import java.util.List;

public class Compiler {
    private final List<StepDefinition> stepdefs;
    private final LocalizedXStreams xStreams;

    public Compiler(List<StepDefinition> stepdefs) {
        this.stepdefs = stepdefs;
        this.xStreams = new LocalizedXStreams(getClass().getClassLoader());
    }

    public TestCase compile(Pickle pickle) {
        I18n i18n = new I18n("en");
        LocalizedXStreams.LocalizedXStream xStream = xStreams.get(i18n.getLocale());
        TestCase testCase = new TestCase();
        for (PickleStep pickleStep : pickle.getSteps()) {
            testCase.add(compile(pickleStep, xStream));
        }
        return testCase;
    }

    private TestStep compile(PickleStep pickleStep, LocalizedXStreams.LocalizedXStream xStream) {
        for (StepDefinition stepdef : stepdefs) {
            List<Argument> arguments = stepdef.matchedArguments(pickleStep.getText());
            if (arguments != null) {
                return new PickleTestStep(pickleStep, stepdef, arguments, xStream);
            }
        }
        return null;
    }
}
