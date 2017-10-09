package cucumber.runtime.formatter;

import gherkin.pickles.PickleStep;

import org.mockito.ArgumentMatcher;

public class PickleStepMatcher extends ArgumentMatcher<PickleStep> {
    private final String textToMatch;

    public PickleStepMatcher(String text) {
        this.textToMatch = text;
    }

    @Override
    public boolean matches(Object argument) {
        return argument instanceof PickleStep && (((PickleStep)argument).getText().contains(textToMatch));
    }
}
