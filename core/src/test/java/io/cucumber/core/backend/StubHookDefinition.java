package io.cucumber.core.backend;

import java.util.function.Consumer;

public class StubHookDefinition implements HookDefinition {

    private static final String STUBBED_LOCATION_WITH_DETAILS = "{stubbed location with details}";
    private final String location;
    private final RuntimeException exception;
    private final Consumer<TestCaseState> action;

    public StubHookDefinition(String location, RuntimeException exception, Consumer<TestCaseState> action) {
        this.location = location;
        this.exception = exception;
        this.action = action;
    }

    public StubHookDefinition(String location, Consumer<TestCaseState> action) {
        this(location, null, action);
    }

    public StubHookDefinition() {
        this(STUBBED_LOCATION_WITH_DETAILS, null, null);
    }

    public StubHookDefinition(Consumer<TestCaseState> action) {
        this(STUBBED_LOCATION_WITH_DETAILS, null, action);
    }

    public StubHookDefinition(RuntimeException exception) {
        this(STUBBED_LOCATION_WITH_DETAILS, exception, null);
    }

    public StubHookDefinition(String location) {
        this(location, null, null);
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
        return location;
    }

}
