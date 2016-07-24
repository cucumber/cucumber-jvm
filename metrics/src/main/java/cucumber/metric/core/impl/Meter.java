package cucumber.metric.core.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import cucumber.metric.core.Metered;

public class Meter implements Metered {

    private static final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(5);

    private final EWMA m1Rate = EWMA.oneMinuteEWMA();
    private final EWMA m5Rate = EWMA.fiveMinuteEWMA();
    private final EWMA m15Rate = EWMA.fifteenMinuteEWMA();

    private final LongAdder count = new LongAdder();
    private final long startTime;
    private final AtomicLong lastTick;
    private final Clock clock;
    private long nextAvailableTime;
    private final long downtime;

    /**
     * Creates a new {@link Meter}.
     */
    public Meter() {
        this(-1);
    }

    /**
     * Creates a new {@link Meter}.
     */
    public Meter(long downtime) {
        this(downtime, Clock.defaultClock());
    }

    /**
     * Creates a new {@link Meter}.
     *
     * @param clock
     *            the clock to use for the meter ticks
     */
    public Meter(final long downtime, Clock clock) {
        this.clock = clock;
        this.startTime = this.clock.getTick();
        this.lastTick = new AtomicLong(startTime);
        this.downtime = downtime;
        if (this.downtime != -1) {
            this.nextAvailableTime = this.startTime + this.downtime;
        } else {
            this.nextAvailableTime = Long.MAX_VALUE;
        }

    }

    @Override
    public long getCount() {
        return count.sum();
    }

    /**
     * Mark the occurrence of an event.
     */
    public void mark() {
        mark(1);
    }

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n
     *            the number of events
     */
    public void mark(long n) {
        tickIfNecessary();
        count.add(n);
        m1Rate.update(n);
        m5Rate.update(n);
        m15Rate.update(n);
    }

    private void tickIfNecessary() {
        final long oldTick = lastTick.get();
        final long newTick = clock.getTick();
        final long age = newTick - oldTick;
        if (age > TICK_INTERVAL) {
            final long newIntervalStartTick = newTick - age % TICK_INTERVAL;
            if (lastTick.compareAndSet(oldTick, newIntervalStartTick)) {
                final long requiredTicks = age / TICK_INTERVAL;
                for (long i = 0; i < requiredTicks; i++) {
                    m1Rate.tick();
                    m5Rate.tick();
                    m15Rate.tick();
                }
            }
        }
    }

    public void waitIfNecessaryAndUpdateNextAvailableTime() {
        final long newTick = clock.getTick();
        long wait = this.nextAvailableTime - newTick;
        try {
            TimeUnit.NANOSECONDS.sleep(wait);
        } catch (InterruptedException e) {
        }
        this.nextAvailableTime = clock.getTick() + this.downtime;
    }

}
