package cucumber.runtime.groovy;

import gherkin.TagExpression;
import groovy.lang.Closure;

import java.util.Collection;
import java.util.List;

import cucumber.runtime.HookDefinition;
import cucumber.runtime.ScenarioResult;

public class GroovyHookDefinition implements HookDefinition {
    private Closure closure;
    private TagExpression tagExpression;
    private int order;

    public GroovyHookDefinition(Closure closure, List<String> tags, int order) {
        this.closure = closure;
        this.tagExpression = new TagExpression(tags);
        this.order = order;
    }

    @Override
    public void execute(ScenarioResult scenarioResult) throws Throwable {
        closure.call();
    }

    @Override
    public boolean matches(Collection<String> tags) {
        return tagExpression.eval(tags);
    }

    @Override
    public int getOrder() {
        return order;
    }
}
