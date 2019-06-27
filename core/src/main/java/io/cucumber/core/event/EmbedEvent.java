package io.cucumber.core.event;

import java.time.Instant;

public final class EmbedEvent extends TestCaseEvent {
    public final byte[] data;
    public final String mimeType;

    public EmbedEvent(Instant timeInstant, TestCase testCase, byte[] data, String mimeType) {
        super(timeInstant, testCase);
        this.data = data;
        this.mimeType = mimeType;
    }

}
