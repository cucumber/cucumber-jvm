package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.time.Instant;
import java.util.Objects;

@API(status = API.Status.STABLE)
public final class WriteEvent extends TestCaseEvent {

    private final String text;

    public WriteEvent(Instant timeInstant, TestCase testCase, String text) {
        super(timeInstant, testCase);
        this.text = Objects.requireNonNull(text);
    }

    public String getText() {
        return text;
    }

}
