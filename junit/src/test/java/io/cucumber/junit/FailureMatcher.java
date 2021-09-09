package io.cucumber.junit;

import org.junit.runner.notification.Failure;
import org.mockito.ArgumentMatcher;

final class FailureMatcher implements ArgumentMatcher<Failure> {

    private final String name;

    FailureMatcher(String name) {
        this.name = name;
    }

    @Override
    public boolean matches(Failure argument) {
        return argument != null && argument.getDescription().getDisplayName().equals(name);
    }

}
