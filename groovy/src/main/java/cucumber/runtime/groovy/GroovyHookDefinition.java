package cucumber.runtime.groovy;

import cucumber.api.Scenario;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.Timeout;
import groovy.lang.Closure;

public class GroovyHookDefinition implements HookDefinition {
    private final String tagExpression;
    private final int timeoutMillis;
    private final Closure body;
    private final GroovyBackend backend;
    private final StackTraceElement location;

    public GroovyHookDefinition(String tagExpression, int timeoutMillis, Closure body, StackTraceElement location, GroovyBackend backend) {
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
    public void execute(final Scenario scenario) throws Throwable {
        Timeout.timeout(new Timeout.Callback<Object>() {
            @Override
            public Object call() throws Throwable {
                backend.invoke(body, new Object[]{scenario});
                return null;
            }
        }, timeoutMillis);
    }

    @Override
    public String getTagExpression() {
        return tagExpression;
    }

    @Override
    public int getOrder() {
        return location.getFileName() == "env.groovy" ? -1 : 0;
    }
}

