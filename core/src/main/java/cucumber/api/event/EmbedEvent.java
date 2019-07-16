package cucumber.api.event;

import cucumber.api.TestCase;

public final class EmbedEvent extends TestCaseEvent {
    public final byte[] data;
    public final String mimeType;
    public final String name;

    @Deprecated
    public EmbedEvent(Long timeStamp, TestCase testCase, byte[] data, String mimeType) {
        this(timeStamp, 0, testCase, data, mimeType);
    }

    public EmbedEvent(Long timeStamp, long timeStampMillis, TestCase testCase, byte[] data, String mimeType) {
        super(timeStamp, timeStampMillis, testCase);
        this.data = data;
        this.mimeType = mimeType;
        this.name = null;
    }

    public EmbedEvent(Long timeStamp, long timeStampMillis, TestCase testCase, byte[] data, String mimeType, String name) {
        super(timeStamp, timeStampMillis, testCase);
        this.data = data;
        this.mimeType = mimeType;
        this.name = name;
    }

}
