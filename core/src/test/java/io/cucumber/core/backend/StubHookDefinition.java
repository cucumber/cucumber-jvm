package io.cucumber.core.backend;

import java.util.function.Consumer;

public class StubHookDefinition implements HookDefinition {

    private final RuntimeException exception;
    private final Consumer<TestCaseState> action;

    public StubHookDefinition(RuntimeException exception, Consumer<TestCaseState> action) {
        this.exception = exception;
        this.action = action;
    }

    public StubHookDefinition() {
        this(null, null);
    }

    public StubHookDefinition(Consumer<TestCaseState> action) {
        this(null, action);
    }

    public StubHookDefinition(RuntimeException exception) {
        this(exception, null);
    }

    @Override
    public void execute(TestCaseState state) {
        if (action != null) {
            action.accept(state);
        }
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    public String getTagExpression() {
        return "";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return false;
    }

    @Override
    public String getLocation() {
        return "{stubbed location with details}";
    }

}
