package cucumber.api.event;

abstract class TimeStampedEvent implements Event {
    private final Long timeStamp;
    private final Thread thread = Thread.currentThread();

    TimeStampedEvent(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public Long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public Thread getThread() {
        return thread;
    }
}
