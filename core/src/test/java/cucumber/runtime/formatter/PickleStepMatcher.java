package cucumber.runtime.formatter;

import gherkin.pickles.PickleStep;

import org.mockito.ArgumentMatcher;

public class PickleStepMatcher implements ArgumentMatcher<PickleStep> {
    private final String textToMatch;

    public PickleStepMatcher(String text) {
        this.textToMatch = text;
    }

    @Override
    public boolean matches(PickleStep argument) {
        return argument != null && (argument.getText().contains(textToMatch));
    }
}
