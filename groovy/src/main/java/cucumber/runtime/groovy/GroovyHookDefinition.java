package cucumber.runtime.groovy;

import cucumber.runtime.HookDefinition;
import cucumber.runtime.ScenarioResult;
import gherkin.TagExpression;
import gherkin.formatter.model.Tag;
import groovy.lang.Closure;

import java.util.Collection;

public class GroovyHookDefinition implements HookDefinition {
    private final Closure body;
    private final TagExpression tagExpression;
    private final GroovyBackend backend;
    private final StackTraceElement location;

    public GroovyHookDefinition(Closure body, TagExpression tagExpression, StackTraceElement location, GroovyBackend backend) {
        this.body = body;
        this.tagExpression = tagExpression;
        this.location = location;
        this.backend = backend;
    }

    @Override
    public String getLocation(boolean detail) {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    @Override
    public void execute(ScenarioResult scenarioResult) throws Throwable {
        backend.invoke(body, new Object[]{scenarioResult});
    }

    @Override
    public boolean matches(Collection<Tag> tags) {
        return tagExpression.eval(tags);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
