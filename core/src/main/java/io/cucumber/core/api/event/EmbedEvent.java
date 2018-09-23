package io.cucumber.core.api.event;

public final class EmbedEvent extends TestCaseEvent {
    public final byte[] data;
    public final String mimeType;

    public EmbedEvent(Long timeStamp, TestCase testCase, byte[] data, String mimeType) {
        super(timeStamp, testCase);
        this.data = data;
        this.mimeType = mimeType;
    }

}
