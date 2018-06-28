package cucumber.runtime.formatter;

import cucumber.api.Plugin;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;

public class StubFormatter implements Plugin, EventListener {

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        throw new UnsupportedOperationException();
    }

}
