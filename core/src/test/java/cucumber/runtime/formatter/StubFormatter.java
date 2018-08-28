package cucumber.runtime.formatter;

import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;

public class StubFormatter implements EventListener {

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        throw new UnsupportedOperationException();
    }

}
