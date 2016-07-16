package cucumber.runtime.java;

import cucumber.api.Scenario;
import cucumber.api.java8.HookBody;
import cucumber.api.java8.HookNoArgsBody;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.Timeout;
import cucumber.runtime.TagExpression;
import gherkin.pickles.PickleTag;

import java.util.Collection;

import static java.util.Arrays.asList;

public class Java8HookDefinition implements HookDefinition {
    private final TagExpression tagExpression;
    private final int order;
    private final long timeoutMillis;
    private final HookNoArgsBody hookNoArgsBody;
    private final HookBody hookBody;
    private final StackTraceElement location;

    private Java8HookDefinition(String[] tagExpressions, int order, long timeoutMillis, HookBody hookBody, HookNoArgsBody hookNoArgsBody) {
        this.order = order;
        this.timeoutMillis = timeoutMillis;
        this.tagExpression = new TagExpression(asList(tagExpressions));
        this.hookBody = hookBody;
        this.hookNoArgsBody = hookNoArgsBody;
        this.location = new Exception().getStackTrace()[3];
    }

    public Java8HookDefinition(String[] tagExpressions, int order, long timeoutMillis, HookBody hookBody) {
        this(tagExpressions, order, timeoutMillis, hookBody, null);
    }

    public Java8HookDefinition(String[] tagExpressions, int order, long timeoutMillis, HookNoArgsBody hookNoArgsBody) {
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
                    hookNoArgsBody.accept();
                }
                return null;

            }
        }, timeoutMillis);
    }

    @Override
    public boolean matches(Collection<PickleTag> tags) {
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
