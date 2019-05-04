package io.cucumber.core.api.event;

import java.time.Instant;

public final class EmbedEvent extends TestCaseEvent {
    public final byte[] data;
    public final String mimeType;

    //gazler
//    @Deprecated
//    public EmbedEvent(Long timeStamp, TestCase testCase, byte[] data, String mimeType) {
//        this(timeStamp, 0, testCase, data, mimeType);
//    }
//
//    public EmbedEvent(Long timeStamp, long timeStampMillis, TestCase testCase, byte[] data, String mimeType) {
//        super(timeStamp, timeStampMillis, testCase);
//        this.data = data;
//        this.mimeType = mimeType;
//    }
    
    public EmbedEvent(Instant timeInstant, TestCase testCase, byte[] data, String mimeType) {
        super(timeInstant, testCase);
        this.data = data;
        this.mimeType = mimeType;
    }

}
