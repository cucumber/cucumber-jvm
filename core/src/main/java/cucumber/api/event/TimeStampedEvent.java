package cucumber.api.event;

abstract class TimeStampedEvent implements Event {
    private Long timeStamp;

    public TimeStampedEvent(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public Long getTimeStamp() {
        return timeStamp;
    }
}
