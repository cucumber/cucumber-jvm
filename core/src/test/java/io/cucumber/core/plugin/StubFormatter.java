package io.cucumber.core.plugin;

import io.cucumber.core.api.plugin.EventListener;
import io.cucumber.core.api.event.EventPublisher;

public class StubFormatter implements EventListener {

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        throw new UnsupportedOperationException();
    }

}
