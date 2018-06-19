package cucumber.runtime.formatter;

import cucumber.api.Plugin;
import cucumber.api.event.ConcurrentEventListener;
import cucumber.api.event.EventPublisher;

final class NullFormatter implements ConcurrentEventListener, Plugin {
    public NullFormatter() {
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
    }
}
