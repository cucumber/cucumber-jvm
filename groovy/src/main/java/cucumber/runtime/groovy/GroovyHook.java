package cucumber.runtime.groovy;

import cucumber.runtime.HookDefinition;
import cucumber.runtime.ScenarioResult;
import gherkin.TagExpression;
import groovy.lang.Closure;

import java.util.Collection;

public class GroovyHook implements HookDefinition {
    private final Closure body;
    private final TagExpression tagExpression;
    private final GroovyBackend backend;

    public GroovyHook(Closure body, TagExpression tagExpression, GroovyBackend backend) {
        this.body = body;
        this.tagExpression = tagExpression;
        this.backend = backend;
    }

    @Override
    public void execute(ScenarioResult scenarioResult) throws Throwable {
        backend.invoke(body, new Object[]{scenarioResult});
    }

    @Override
    public boolean matches(Collection<String> tags) {
        return tagExpression.eval(tags);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
