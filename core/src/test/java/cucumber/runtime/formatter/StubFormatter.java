package cucumber.runtime.formatter;

import cucumber.api.event.EventPublisher;
import cucumber.api.formatter.Formatter;

public class StubFormatter implements Formatter {

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        throw new UnsupportedOperationException();
    }

}
