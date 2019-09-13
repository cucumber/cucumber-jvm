package io.cucumber.core.runner;

class StepDefinitionEvent implements io.cucumber.plugin.event.StepDefinition {

    private final io.cucumber.core.backend.StepDefinition delegate;

    StepDefinitionEvent(io.cucumber.core.backend.StepDefinition delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getLocation() {
        return delegate.getLocation();
    }

    @Override
    public String getPattern() {
        return delegate.getPattern();
    }
}
