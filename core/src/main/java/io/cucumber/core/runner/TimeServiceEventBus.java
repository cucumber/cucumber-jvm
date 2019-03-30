package io.cucumber.core.runner;

import io.cucumber.core.event.AbstractEventBus;

public final class TimeServiceEventBus extends AbstractEventBus {
    private final TimeService stopWatch;

    public TimeServiceEventBus(TimeService stopWatch) {
        this.stopWatch = stopWatch;
    }

    @Override
    public Long getTime() {
        return stopWatch.time();
    }

    @Override
    public Long getTimeStampMillis() {
        return stopWatch.timeStampMillis();
    }
}
