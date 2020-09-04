package io.cucumber.core.backend;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.TestCaseState;

public class StubHookDefinition implements HookDefinition {

    private final RuntimeException exception;

    public StubHookDefinition() {
        this(null);
    }

    public StubHookDefinition(RuntimeException exception) {
        this.exception = exception;
    }

    @Override
    public void execute(TestCaseState state) {
        if (exception == null) {
            return;
        }
        throw exception;
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
