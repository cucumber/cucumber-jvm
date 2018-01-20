package cucumber.api.event;

abstract class TimeStampedEvent implements Event {
    private final Long timeStamp;

    TimeStampedEvent(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public Long getTimeStamp() {
        return timeStamp;
    }
}
