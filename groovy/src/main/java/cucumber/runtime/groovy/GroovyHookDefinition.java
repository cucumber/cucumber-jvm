package cucumber.runtime.groovy;

import cucumber.runtime.HookDefinition;
import cucumber.runtime.ScenarioResult;
import cucumber.runtime.Timeout;
import gherkin.TagExpression;
import gherkin.formatter.model.Tag;
import groovy.lang.Closure;

import java.util.Collection;

public class GroovyHookDefinition implements HookDefinition {
    private final TagExpression tagExpression;
    private final int timeoutMillis;
    private final Closure body;
    private final GroovyBackend backend;
    private final StackTraceElement location;

    public GroovyHookDefinition(TagExpression tagExpression, int timeoutMillis, Closure body, StackTraceElement location, GroovyBackend backend) {
        this.tagExpression = tagExpression;
        this.timeoutMillis = timeoutMillis;
        this.body = body;
        this.location = location;
        this.backend = backend;
    }

    @Override
    public String getLocation(boolean detail) {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    @Override
    public void execute(final ScenarioResult scenarioResult) throws Throwable {
        Timeout.timeout(new Timeout.Callback<Object>() {
            @Override
            public Object call() throws Throwable {
                backend.invoke(body, new Object[]{scenarioResult});
                return null;
            }
        }, timeoutMillis);
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
