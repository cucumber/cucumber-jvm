package cucumber.api.event;

public class EmbedEvent implements Event {
    public final byte[] data;
    public final String mimeType;

    public EmbedEvent(byte[] data, String mimeType) {
        this.data = data;
        this.mimeType = mimeType;
    }

}
