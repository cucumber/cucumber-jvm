package io.cucumber.java8;

import gherkin.pickles.PickleTag;
import io.cucumber.core.api.Scenario;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.filter.TagPredicate;
import io.cucumber.core.runtime.Invoker;

import java.util.Collection;

final class Java8HookDefinition extends AbstractGlueDefinition implements HookDefinition {
    private final TagPredicate tagPredicate;
    private final int order;
    private final long timeoutMillis;

    private Java8HookDefinition(String tagExpressions, int order, long timeoutMillis, Object body) {
        super(body, new Exception().getStackTrace()[3]);
        this.order = order;
        this.timeoutMillis = timeoutMillis;
        this.tagPredicate = new TagPredicate(tagExpressions);
    }

    Java8HookDefinition(String tagExpressions, int order, long timeoutMillis, HookBody hookBody) {
        this(tagExpressions, order, timeoutMillis, (Object) hookBody);
    }

    Java8HookDefinition(String tagExpressions, int order, long timeoutMillis, HookNoArgsBody hookNoArgsBody) {
        this(tagExpressions, order, timeoutMillis, (Object) hookNoArgsBody);
    }

    @Override
    public void execute(final Scenario scenario) throws Throwable {
        Object[] args;
        if (method.getParameterCount() == 0) {
            args = new Object[0];
        } else {
            args = new Object[]{scenario};
        }
        Invoker.invoke(body, method, timeoutMillis, args);
    }

    @Override
    public boolean matches(Collection<PickleTag> tags) {
        return tagPredicate.apply(tags);
    }

    @Override
    public int getOrder() {
        return order;
    }

}
