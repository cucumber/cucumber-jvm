package cucumber.runtime.formatter;

import gherkin.formatter.model.Step;

import org.mockito.ArgumentMatcher;

public class StepMatcher extends ArgumentMatcher<Step> {
    private final String nameToMatch;

    public StepMatcher(String name) {
        this.nameToMatch = name;
    }

    @Override
    public boolean matches(Object argument) {
        return argument instanceof Step && (((Step)argument).getName().contains(nameToMatch));
    }
}
