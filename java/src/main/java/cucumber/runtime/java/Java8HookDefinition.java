package cucumber.runtime.java;

import cucumber.api.Scenario;
import cucumber.api.java8.HookBody;
import cucumber.runtime.HookDefinition;
import gherkin.formatter.model.Tag;

import java.util.Collection;

public class Java8HookDefinition implements HookDefinition {
    public Java8HookDefinition(Object body, String[] tagExpression, int order, long timeoutMillis, ObjectFactory objectFactory) {
    }

    @Override
    public String getLocation(boolean detail) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(Scenario scenario) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean matches(Collection<Tag> tags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getOrder() {
        throw new UnsupportedOperationException();
    }
}
