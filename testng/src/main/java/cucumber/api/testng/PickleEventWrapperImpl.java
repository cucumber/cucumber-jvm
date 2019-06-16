package cucumber.api.testng;

import gherkin.events.PickleEvent;

/**
 * @deprecated use {@link io.cucumber.testng.PickleEventWrapper} instead
 */
@Deprecated
class PickleEventWrapperImpl implements PickleEventWrapper {

    private final io.cucumber.testng.PickleEventWrapper delegate;

    PickleEventWrapperImpl(io.cucumber.testng.PickleEventWrapper delegate) {
        this.delegate = delegate;
    }

    public PickleEvent getPickleEvent() {
        return delegate.getPickleEvent();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
