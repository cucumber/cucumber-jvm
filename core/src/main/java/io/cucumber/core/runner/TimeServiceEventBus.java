package io.cucumber.core.runner;

import java.time.Instant;

import io.cucumber.core.event.AbstractEventBus;

public final class TimeServiceEventBus extends AbstractEventBus {
    private final TimeService stopWatch;

    public TimeServiceEventBus(TimeService stopWatch) {
        this.stopWatch = stopWatch;
    }

    //gazler
//    @Override
//    public Long getTime() {
//        return stopWatch.time();
//    }
//
//    @Override
//    public Long getTimeMillis() {
//        return stopWatch.timeMillis();
//    }
    
    @Override
    public Instant getTimeInstant() {
        return stopWatch.timeInstant();
    }
}
