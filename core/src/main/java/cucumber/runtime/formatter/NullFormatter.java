package cucumber.runtime.formatter;

import cucumber.api.Plugin;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;

final class NullFormatter implements Plugin, EventListener {
    public NullFormatter() {
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
    }
}
