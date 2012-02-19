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

    public GroovyHookDefinition(Closure body, TagExpression tagExpression, GroovyBackend backend) {
        this.body = body;
        this.tagExpression = tagExpression;
        this.backend = backend;
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
