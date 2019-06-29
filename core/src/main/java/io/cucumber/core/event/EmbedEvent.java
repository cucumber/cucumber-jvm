package io.cucumber.core.event;

import org.apiguardian.api.API;

import java.time.Instant;
import java.util.Objects;

@API(status = API.Status.STABLE)
public final class EmbedEvent extends TestCaseEvent {
    private final byte[] data;
    private final String mimeType;

    public EmbedEvent(Instant timeInstant, TestCase testCase, byte[] data, String mimeType) {
        super(timeInstant, testCase);
        this.data = Objects.requireNonNull(data);
        this.mimeType = Objects.requireNonNull(mimeType);
    }

    public byte[] getData() {
        return data;
    }

    public String getMimeType() {
        return mimeType;
    }
}
