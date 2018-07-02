package cucumber.runtime.formatter;

import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;

final class NullFormatter implements EventListener {
    public NullFormatter() {
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
    }
}
