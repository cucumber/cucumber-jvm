package io.cucumber.core.api.event;

import java.time.Instant;

public final class TestRunStarted extends TimeStampedEvent {

    //gazler
//    @Deprecated
//    public TestRunStarted(Long timeStamp) {
//        this(timeStamp, 0);
//    }
//
//    public TestRunStarted(Long timeStamp, long timeStampMillis) {
//        super(timeStamp, timeStampMillis);
//    }
    
    public TestRunStarted(Instant timeInstant) {
        super(timeInstant);
    }
}
