package io.cucumber.core.plugin;

import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.EventListener;

public class StubFormatter implements EventListener {

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        throw new UnsupportedOperationException();
    }

}
