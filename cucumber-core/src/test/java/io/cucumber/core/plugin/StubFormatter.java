package io.cucumber.core.plugin;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;

class StubFormatter implements EventListener {

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        throw new UnsupportedOperationException();
    }

}
