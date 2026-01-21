package io.cucumber.java;

import io.cucumber.core.backend.Step;

/**
 * Internal wrapper that implements the public {@link io.cucumber.java.Step} interface by
 * delegating to the internal step representation.
 */
final class StepInfo implements io.cucumber.java.Step {

    private final Step delegate;

    StepInfo(Step delegate) {
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

}
