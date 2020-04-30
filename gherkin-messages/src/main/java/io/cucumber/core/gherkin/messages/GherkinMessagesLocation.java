package io.cucumber.core.gherkin.messages;

import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.Location;

final class GherkinMessagesLocation {

    static Location from(Messages.Location location) {
        return new Location(location.getLine(), location.getColumn());
    }

}
