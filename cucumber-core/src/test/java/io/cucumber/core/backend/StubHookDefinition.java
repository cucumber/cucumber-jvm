package io.cucumber.core.backend;

import java.util.Optional;
import java.util.function.Consumer;

public class StubHookDefinition implements HookDefinition {

    private static final String STUBBED_LOCATION_WITH_DETAILS = "{stubbed location with details}";
    private final Located location;
    private final RuntimeException exception;
    private final Consumer<TestCaseState> action;
    private final HookType hookType;

    public StubHookDefinition(
            Located location, RuntimeException exception, Consumer<TestCaseState> action, HookType hookType
    ) {
        this.location = location;
        this.exception = exception;
        this.action = action;
        this.hookType = hookType;
    }

    public StubHookDefinition(SourceReference location, HookType hookType, Consumer<TestCaseState> action) {
        this(new StubLocation(location), null, action, hookType);
    }

    public StubHookDefinition(Consumer<TestCaseState> action) {
        this(new StubLocation(STUBBED_LOCATION_WITH_DETAILS), null, action, null);
    }

    public StubHookDefinition(RuntimeException exception) {
        this(new StubLocation(STUBBED_LOCATION_WITH_DETAILS), exception, null, null);
    }

    public StubHookDefinition(SourceReference sourceReference, HookType hookType) {
        this(new StubLocation(sourceReference), null, null, hookType);
    }

    public StubHookDefinition(SourceReference sourceReference, HookType hookType, RuntimeException exception) {
        this(new StubLocation(sourceReference), exception, null, hookType);
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
        return location.getLocation();
    }

    @Override
    public Optional<SourceReference> getSourceReference() {
        return location.getSourceReference();
    }

    @Override
    public Optional<HookType> getHookType() {
        return Optional.ofNullable(hookType);
    }
}
