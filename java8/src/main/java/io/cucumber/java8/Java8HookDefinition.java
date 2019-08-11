package io.cucumber.java8;

import io.cucumber.core.api.Scenario;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.runtime.Invoker;

import static java.util.Objects.requireNonNull;

final class Java8HookDefinition extends AbstractGlueDefinition implements HookDefinition {
    private final String tagExpression;
    private final int order;
    private final long timeoutMillis;

    private Java8HookDefinition(String tagExpression, int order, long timeoutMillis, Object body) {
        super(body, new Exception().getStackTrace()[3]);
        this.order = order;
        this.timeoutMillis = timeoutMillis;
        this.tagExpression = requireNonNull(tagExpression, "tag-expression may not be null");
    }

    Java8HookDefinition(String tagExpression, int order, long timeoutMillis, HookBody hookBody) {
        this(tagExpression, order, timeoutMillis, (Object) hookBody);
    }

    Java8HookDefinition(String tagExpression, int order, long timeoutMillis, HookNoArgsBody hookNoArgsBody) {
        this(tagExpression, order, timeoutMillis, (Object) hookNoArgsBody);
    }

    @SuppressWarnings("deprecation")
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
    public String getTagExpression() {
        return tagExpression;
    }

    @Override
    public int getOrder() {
        return order;
    }

}
