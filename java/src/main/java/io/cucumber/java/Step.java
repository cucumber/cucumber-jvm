package io.cucumber.java;

import io.cucumber.core.backend.PickleStep;
import org.apiguardian.api.API;

/**
 * BeforeStep or AfterStep Hooks that declare a parameter of this type will
 * receive an instance of this class.
 */
@API(status = API.Status.EXPERIMENTAL)
public final class Step implements PickleStep {

    final io.cucumber.core.backend.PickleStep delegate;

    public Step(final PickleStep delegate) {
        this.delegate = delegate;
    }

    @Override
    public PickleStep.Keyword getKeyword() {
        return delegate.getKeyword();
    }

    @Override
    public Object[] getArguments() {
        return delegate.getArguments();
    }

}
