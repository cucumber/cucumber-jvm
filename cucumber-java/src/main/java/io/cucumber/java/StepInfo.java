package io.cucumber.java;

import io.cucumber.core.backend.PickleStep;

/**
 * Internal wrapper that implements the public {@link Step} interface by
 * delegating to the internal step representation.
 */
final class StepInfo implements Step {

    private final PickleStep delegate;

    StepInfo(PickleStep delegate) {
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
