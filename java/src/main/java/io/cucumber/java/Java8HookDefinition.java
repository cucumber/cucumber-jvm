package io.cucumber.java;

import static java.util.Arrays.asList;

import io.cucumber.core.api.Scenario;
import io.cucumber.java.api.HookBody;
import io.cucumber.java.api.HookNoArgsBody;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.filter.TagPredicate;
import gherkin.pickles.PickleTag;
import io.cucumber.core.runtime.Invoker;

import java.util.Collection;

public class Java8HookDefinition implements HookDefinition {
    private final TagPredicate tagPredicate;
    private final int order;
    private final long timeoutMillis;
    private final HookNoArgsBody hookNoArgsBody;
    private final HookBody hookBody;
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

    @Override
    public String getLocation(boolean detail) {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    @Override
    public void execute(final Scenario scenario) throws Throwable {
        Invoker.timeout(() -> {
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
}
