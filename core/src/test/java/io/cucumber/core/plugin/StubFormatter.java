package io.cucumber.core.plugin;

import io.cucumber.event.EventPublisher;
import io.cucumber.plugin.EventListener;

public class StubFormatter implements EventListener {

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        throw new UnsupportedOperationException();
    }

}
