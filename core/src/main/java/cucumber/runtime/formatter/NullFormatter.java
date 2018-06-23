package cucumber.runtime.formatter;

import cucumber.api.event.EventPublisher;
import cucumber.api.formatter.ConcurrentFormatter;

final class NullFormatter implements ConcurrentFormatter {
    public NullFormatter() {
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
    }
}
