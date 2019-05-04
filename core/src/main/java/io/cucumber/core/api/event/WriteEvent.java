package io.cucumber.core.api.event;

import java.time.Instant;

public final class WriteEvent extends TestCaseEvent {
    public final String text;

    //gazler
//    @Deprecated
//    public WriteEvent(Long timeStamp, TestCase testCase, String text) {
//        this(timeStamp, 0, testCase, text);
//    }
//
//    public WriteEvent(Long timeStamp, long timeStampMillis, TestCase testCase, String text) {
//        super(timeStamp, timeStampMillis, testCase);
//        this.text = text;
//    }
    
    public WriteEvent(Instant timeInstant, TestCase testCase, String text) {
        super(timeInstant, testCase);
        this.text = text;
    }
}
