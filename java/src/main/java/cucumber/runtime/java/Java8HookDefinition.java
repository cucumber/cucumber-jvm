package cucumber.runtime.java;

import cucumber.api.Scenario;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.Timeout;
import gherkin.TagExpression;
import gherkin.formatter.model.Tag;

import java.util.Collection;
import java.util.function.Consumer;

import static java.util.Arrays.asList;

public class Java8HookDefinition implements HookDefinition {
    private final TagExpression tagExpression;
    private final int order;
    private final long timeoutMillis;
    private final Runnable hookNoArgsBody;
    private final Consumer<Scenario> hookBody;
    private final StackTraceElement location;

    private Java8HookDefinition(String[] tagExpressions, int order, long timeoutMillis, Consumer<Scenario> hookBody, Runnable hookNoArgsBody) {
        this.order = order;
        this.timeoutMillis = timeoutMillis;
        this.tagExpression = new TagExpression(asList(tagExpressions));
        this.hookBody = hookBody;
        this.hookNoArgsBody = hookNoArgsBody;
        this.location = new Exception().getStackTrace()[3];
    }

    public Java8HookDefinition(String[] tagExpressions, int order, long timeoutMillis, Consumer<Scenario> hookBody) {
        this(tagExpressions, order, timeoutMillis, hookBody, null);
    }

    public Java8HookDefinition(String[] tagExpressions, int order, long timeoutMillis, Runnable hookNoArgsBody) {
        this(tagExpressions, order, timeoutMillis, null, hookNoArgsBody);
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
                if (hookBody != null) {
                    hookBody.accept(scenario);
                } else {
                    hookNoArgsBody.run();
                }
                return null;

            }
        }, timeoutMillis);
    }

    @Override
    public boolean matches(Collection<Tag> tags) {
        return tagExpression.evaluate(tags);
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public boolean isScenarioScoped() {
        return true;
    }
}
