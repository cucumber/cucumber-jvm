package cucumber.runtime.junit;

import org.junit.runner.Description;
import org.mockito.ArgumentMatcher;

final class DescriptionMatcher implements ArgumentMatcher<Description> {
    private String name;

    DescriptionMatcher(String name) {
        this.name = name;
    }

    @Override
    public boolean matches(Description argument) {
        return argument != null && argument.getDisplayName().equals(name);
    }

}
