package io.cucumber.core.backend;

public class StubStaticHookDefinition implements StaticHookDefinition {

    private static final String STUBBED_LOCATION_WITH_DETAILS = "{stubbed location with details}";
    private final String location;
    private final RuntimeException exception;
    private final Runnable action;

    public StubStaticHookDefinition(String location, RuntimeException exception, Runnable action) {
        this.location = location;
        this.exception = exception;
        this.action = action;
    }

    public StubStaticHookDefinition(String location, Runnable action) {
        this(location, null, action);
    }

    public StubStaticHookDefinition() {
        this(STUBBED_LOCATION_WITH_DETAILS, null, null);
    }

    public StubStaticHookDefinition(Runnable action) {
        this(STUBBED_LOCATION_WITH_DETAILS, null, action);
    }

    public StubStaticHookDefinition(RuntimeException exception) {
        this(STUBBED_LOCATION_WITH_DETAILS, exception, null);
    }

    public StubStaticHookDefinition(String location) {
        this(location, null, null);
    }
    @Override
    public void execute() {
        if (action != null) {
            action.run();
        }
        if (exception != null) {
            throw exception;
        }
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
