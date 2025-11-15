package io.cucumber.core.gherkin.messages;

import io.cucumber.plugin.event.Location;

final class GherkinMessagesLocation {

    private GherkinMessagesLocation() {
        // utility class
    }

    static Location from(io.cucumber.messages.types.Location location) {
        return new Location(Math.toIntExact(location.getLine()), Math.toIntExact(location.getColumn().orElse(0)));
    }

}
