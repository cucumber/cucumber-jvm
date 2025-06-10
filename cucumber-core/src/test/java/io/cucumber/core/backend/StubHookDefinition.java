package io.cucumber.core.backend;

import java.util.Optional;
import java.util.function.Consumer;

public class StubHookDefinition implements HookDefinition {

    private static final String STUBBED_LOCATION_WITH_DETAILS = "{stubbed location with details}";
    private final String location;
    private final RuntimeException exception;
    private final Consumer<TestCaseState> action;
    private final SourceReference sourceReference;
    private final HookType hookType;

    public StubHookDefinition(
            String location, RuntimeException exception, Consumer<TestCaseState> action,
            SourceReference sourceReference, HookType hookType
    ) {
        this.location = location;
        this.exception = exception;
        this.action = action;
        this.sourceReference = sourceReference;
        this.hookType = hookType;
    }

    public StubHookDefinition(String location, Consumer<TestCaseState> action) {
        this(location, null, action, null, null);
    }

    public StubHookDefinition() {
        this(STUBBED_LOCATION_WITH_DETAILS, null, null, null, null);
    }

    public StubHookDefinition(Consumer<TestCaseState> action) {
        this(STUBBED_LOCATION_WITH_DETAILS, null, action, null, null);
    }

    public StubHookDefinition(RuntimeException exception) {
        this(STUBBED_LOCATION_WITH_DETAILS, exception, null, null, null);
    }

    public StubHookDefinition(String location) {
        this(location, null, null, null, null);
    }

    public StubHookDefinition(SourceReference sourceReference, HookType hookType) {
        this(null, null, null, sourceReference, hookType);
    }

    public StubHookDefinition(SourceReference sourceReference, HookType hookType, RuntimeException exception) {
        this(null, exception, null, sourceReference, hookType);
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

    @Override
    public Optional<SourceReference> getSourceReference() {
        return Optional.ofNullable(sourceReference);
    }

    @Override
    public Optional<HookType> getHookType() {
        return Optional.ofNullable(hookType);
    }
}
