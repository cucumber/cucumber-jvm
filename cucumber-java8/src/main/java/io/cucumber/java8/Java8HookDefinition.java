package io.cucumber.java8;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.TestCaseState;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

final class Java8HookDefinition extends AbstractGlueDefinition implements HookDefinition {

    private final String tagExpression;
    private final int order;
    private final HookType hookType;

    Java8HookDefinition(HookType hookType, String tagExpression, int order, HookBody hookBody) {
        this(hookType, tagExpression, order, (Object) hookBody);
    }

    private Java8HookDefinition(HookType hookType, String tagExpression, int order, Object body) {
        super(body, new Exception().getStackTrace()[3]);
        this.order = order;
        this.tagExpression = requireNonNull(tagExpression, "tag-expression may not be null");
        this.hookType = requireNonNull(hookType);
    }

    Java8HookDefinition(HookType hookType, String tagExpression, int order, HookNoArgsBody hookNoArgsBody) {
        this(hookType, tagExpression, order, (Object) hookNoArgsBody);
    }

    @Override
    public void execute(final TestCaseState state) {
        Object[] args;
        if (getParameterCount() == 0) {
            args = new Object[0];
        } else {
            args = new Object[] { new io.cucumber.java8.Scenario(state) };
        }
        invokeMethod(args);
    }

    @Override
    public String getTagExpression() {
        return tagExpression;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public Optional<HookType> getHookType() {
        return Optional.of(hookType);
    }
}
