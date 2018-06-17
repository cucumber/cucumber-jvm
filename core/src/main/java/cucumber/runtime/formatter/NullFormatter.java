package cucumber.runtime.formatter;

import cucumber.api.event.ConcurrentEventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.formatter.Formatter;

final class NullFormatter implements Formatter, ConcurrentEventListener {
    public NullFormatter() {
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
    }
}
