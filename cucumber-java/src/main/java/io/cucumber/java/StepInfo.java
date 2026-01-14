package io.cucumber.java;

import io.cucumber.plugin.event.StepArgument;

/**
 * Internal wrapper that implements the public {@link Step} interface by
 * delegating to the internal step representation.
 */
final class StepInfo implements Step {

    private final io.cucumber.plugin.event.Step delegate;

    StepInfo(io.cucumber.plugin.event.Step delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getKeyword() {
        return delegate.getKeyword();
    }

    @Override
    public String getText() {
        return delegate.getText();
    }

    @Override
    public int getLine() {
        return delegate.getLine();
    }

    @Override
    public StepArgument getArgument() {
        return delegate.getArgument();
    }

}
