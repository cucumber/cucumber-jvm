package io.cucumber.core.eventbus;

import io.cucumber.core.exception.CucumberException;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe and collision-free UUID generator for single JVM. This is a
 * sequence generator and each instance has its own counter. This generator is
 * about 100 times faster than #RandomUuidGenerator. If you use Cucumber in
 * multi-JVM setup, you should use #RandomUuidGenerator instead. Note that the
 * UUID version and variant is not guaranteed to be stable.
 */
public class IncrementingUuidGenerator implements UuidGenerator {
    private static final AtomicLong sessionCounter = new AtomicLong(Long.MIN_VALUE);

    private final long sessionId;
    private final AtomicLong counter = new AtomicLong(Long.MIN_VALUE);

    public IncrementingUuidGenerator() {
        sessionId = sessionCounter.incrementAndGet();
    }

    /**
     * Generate a new UUID. Will throw an exception when out of capacity.
     * 
     * @return                   a non-null UUID
     * @throws CucumberException when out of capacity
     */
    @Override
    public UUID get() {
        long leastSigBits = counter.incrementAndGet();
        if (leastSigBits == Long.MAX_VALUE) {
            throw new CucumberException(
                "Out of IncrementingUuidGenerator capacity. Please use the RandomUuidGenerator instead.");
        }
        return new UUID(sessionId, leastSigBits);
    }
}
