package cucumber.runner;

public class TimeServiceEventBus extends AbstractEventBus {
    private final TimeService stopWatch;

    public TimeServiceEventBus(TimeService stopWatch) {
        this.stopWatch = stopWatch;
    }

    @Override
    public Long getTime() {
        return stopWatch.time();
    }
}
