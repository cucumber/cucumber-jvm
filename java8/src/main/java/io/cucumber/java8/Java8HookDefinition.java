package io.cucumber.java8;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.tagexpressions.Expression;

import static java.util.Objects.requireNonNull;

final class Java8HookDefinition extends AbstractGlueDefinition implements HookDefinition {

    private final Expression tagExpression;
    private final int order;

    Java8HookDefinition(Expression tagExpression, int order, HookBody hookBody) {
        this(tagExpression, order, (Object) hookBody);
    }

    private Java8HookDefinition(Expression tagExpression, int order, Object body) {
        super(body, new Exception().getStackTrace()[3]);
        this.order = order;
        this.tagExpression = requireNonNull(tagExpression, "tag-expression may not be null");
    }

    Java8HookDefinition(Expression tagExpression, int order, HookNoArgsBody hookNoArgsBody) {
        this(tagExpression, order, (Object) hookNoArgsBody);
    }

    @Override
    public void execute(final TestCaseState state) {
        Object[] args;
        if (method.getParameterCount() == 0) {
            args = new Object[0];
        } else {
            args = new Object[] { new io.cucumber.java8.Scenario(state) };
        }
        Invoker.invoke(this, body, method, args);
    }

    @Override
    public Expression getTagExpression() {
        return tagExpression;
    }

    @Override
    public int getOrder() {
        return order;
    }

}
