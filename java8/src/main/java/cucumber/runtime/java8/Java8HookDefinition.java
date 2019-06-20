package cucumber.runtime.java8;

import static java.util.Arrays.asList;

import cucumber.api.Scenario;
import cucumber.api.java8.HookBody;
import cucumber.api.java8.HookNoArgsBody;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.ScenarioScoped;
import cucumber.runtime.filter.TagPredicate;
import cucumber.runtime.Timeout;
import gherkin.pickles.PickleTag;

import java.util.Collection;

public class Java8HookDefinition implements HookDefinition, ScenarioScoped {
    private final TagPredicate tagPredicate;
    private final int order;
    private final long timeoutMillis;
    private final HookNoArgsBody hookNoArgsBody;
    private HookBody hookBody;
    private final StackTraceElement location;

    private Java8HookDefinition(String[] tagExpressions, int order, long timeoutMillis, HookBody hookBody, HookNoArgsBody hookNoArgsBody) {
        this.order = order;
        this.timeoutMillis = timeoutMillis;
        this.tagPredicate = new TagPredicate(asList(tagExpressions));
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

    public Java8HookDefinition(String tagExpression, int order, long timeoutMillis, HookBody hookBody) {
        this(new String[]{tagExpression}, order, timeoutMillis, hookBody);
    }

    public Java8HookDefinition(String tagExpression, int order, long timeoutMillis, HookNoArgsBody hookNoArgsBody) {
        this(new String[]{tagExpression}, order, timeoutMillis, hookNoArgsBody);
    }

    @Override
    public String getLocation(boolean detail) {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    @Override
    public void execute(final Scenario scenario) throws Throwable {
        Timeout.timeout(() -> {
            if (hookBody != null) {
                hookBody.accept(scenario);
            } else {
                hookNoArgsBody.accept();
            }
            return null;

        }, timeoutMillis);
    }

    @Override
    public boolean matches(Collection<PickleTag> tags) {
        return tagPredicate.apply(tags);
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public boolean isScenarioScoped() {
        return true;
    }

    @Override
    public void disposeScenarioScope() {
        this.hookBody = null;
    }
}
