package io.cucumber.java;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;

final class TestClock extends Clock {

    private Instant instant;
    private final ZoneId zone;

    TestClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zoneId) {
        return new TestClock(instant, zoneId);
    }

    @Override
    public Instant instant() {
        return instant;
    }

    public void tick(TemporalAmount increment) {
        instant = instant.plus(increment);
    }
}
