package io.cucumber.core.api.event;

public final class TestRunFinished extends TimeStampedEvent {

    @Deprecated
    public TestRunFinished(Long timeStamp) {
        this(timeStamp, 0);
    }

    public TestRunFinished(Long timeStamp, long timeStampMillis) {
        super(timeStamp, timeStampMillis);
    }
}
