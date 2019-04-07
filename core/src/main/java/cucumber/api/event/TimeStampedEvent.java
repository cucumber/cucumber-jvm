package cucumber.api.event;

abstract class TimeStampedEvent implements Event {

    private final Long timeStamp;
    private final long timeStampMillis;

    TimeStampedEvent(Long timeStamp, Long timeStampMillis) {
        this.timeStamp = timeStamp;
        this.timeStampMillis = timeStampMillis;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Returns timestamp in milliseconds of the epoch.
     *
     * @return timestamp in milli seconds
     * @see System#currentTimeMillis()
     */
    public long getTimeStampMillis() {
        return timeStampMillis;
    }
}
