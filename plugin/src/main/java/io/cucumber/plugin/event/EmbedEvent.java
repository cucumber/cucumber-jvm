package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.time.Instant;
import java.util.Objects;

@API(status = API.Status.STABLE)
public final class EmbedEvent extends TestCaseEvent {
    private final byte[] data;
    private final String mediaType;
    public final String name;

    public EmbedEvent(Instant timeInstant, TestCase testCase, byte[] data, String mediaType) {
        super(timeInstant, testCase);
        this.data = Objects.requireNonNull(data);
        this.mediaType = Objects.requireNonNull(mediaType);
        this.name = null;
    }

    public EmbedEvent(Instant timeInstant, TestCase testCase, byte[] data, String mediaType, String name) {
        super(timeInstant, testCase);
        this.data = data;
        this.mediaType = mediaType;
        this.name = name;
    }

    public byte[] getData() {
        return data;
    }

    public String getMediaType() {
        return mediaType;
    }

    /**
     * @deprecated use {@link #getMediaType()}
     *
     * @return media type of the embedding.
     */
    @Deprecated
    public String getMimeType() {
        return mediaType;
    }

    public String getName() {
        return name;
    }
}
