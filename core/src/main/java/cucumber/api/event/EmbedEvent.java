package cucumber.api.event;

public final class EmbedEvent extends TimeStampedEvent {
    public final byte[] data;
    public final String mimeType;

    public EmbedEvent(Long timeStamp, byte[] data, String mimeType) {
        super(timeStamp);
        this.data = data;
        this.mimeType = mimeType;
    }

}
