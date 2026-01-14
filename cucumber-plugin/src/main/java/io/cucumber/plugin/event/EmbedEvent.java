package io.cucumber.plugin.event;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

@API(status = API.Status.STABLE)
public final class EmbedEvent extends TestCaseEvent {

    public final @Nullable String name;
    private final byte[] data;
    private final String mediaType;

    public EmbedEvent(Instant timeInstant, TestCase testCase, byte[] data, String mediaType) {
        this(timeInstant, testCase, data, mediaType, null);
    }

    public EmbedEvent(Instant timeInstant, TestCase testCase, byte[] data, String mediaType, @Nullable String name) {
        super(timeInstant, testCase);
        this.data = requireNonNull(data);
        this.mediaType = requireNonNull(mediaType);
        this.name = name;
    }

    public byte[] getData() {
        return data;
    }

    public String getMediaType() {
        return mediaType;
    }

    /**
     * @return     media type of the embedding.
     * @deprecated use {@link #getMediaType()}
     */
    @Deprecated
    public String getMimeType() {
        return mediaType;
    }

    public @Nullable String getName() {
        return name;
    }

}
