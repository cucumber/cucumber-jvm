package io.cucumber.core.eventbus;

import io.cucumber.core.exception.CucumberException;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe and collision-free UUID generator for single JVM. This is a
 * sequence generator and each instance has its own counter. This generator is
 * about 100 times faster than #RandomUuidGenerator.
 *
 * Properties:
 * - thread-safe
 * - collision-free in the same classloader
 * - almost collision-free in different classloaders / JVMs
 * - UUIDs generated using the instances from the same classloader are sortable
 *
 * UUID version 8 (custom) / variant 2 <a href=
 * "https://www.ietf.org/archive/id/draft-peabody-dispatch-new-uuid-format-04.html#name-uuid-version-8">...</a>
 * <!-- @formatter:off -->
 * |       40 bits      |      8 bits    |  4 bits |    12 bits    |  2 bits | 62 bits |
 * | -------------------| -------------- | ------- | ------------- | ------- | ------- |
 * | LSBs of epoch-time | sessionCounter | version | classloaderId | variant | counter |
 * <!-- @formatter:on -->
 */
public class IncrementingUuidGenerator implements UuidGenerator {
    /**
     * 40 bits mask for the epoch-time part (MSB).
     */
    private static final long MAX_EPOCH_TIME = 0x0ffffffffffL;

    /**
     * 8 bits mask for the session identifier (MSB). Package-private for testing
     * purposes.
     */
    static final long MAX_SESSION_ID = 0xffL;

    /**
     * 62 bits mask for the counter value (LSB)
     */
    static final long MAX_COUNTER_VALUE = 0x3fffffffffffffffL;

    /**
     * Classloader identifier (MSB). The identifier is a pseudo-random number on
     * 12 bits.
     *
     * Since we use a random value (and cut it to 12 bits), there is a small
     * probability that two classloaders generate the same 12 bits random
     * number. This could lead to UUID collisions if the UUID parts (epoch-time,
     * session counter and counter) are the same.
     *
     * The default `classloaderId` (random number cut to 12 bits) has a
     * collision rate of less than 1% when using 10 classloaders (which leads
     * to a much smaller UUID probability thanks to the other dynamic parts
     * of the UUID like epoch-time and counters).
     * If you use multiple classloaders and want to ensure a collision-free UUID
     * generation, you need to provide the `classloaderId` by yourself. You
     * can do so using the {@link #setClassloaderId(int)} method.
     *
     * Note: there is no need to save the Random because it's static.
     */
    @SuppressWarnings("java:S2119")
    static long classloaderId = new Random().nextInt() & 0x0fff;

    /**
     * Session counter to differentiate instances created within a given
     * classloader (MSB).
     */
    static final AtomicLong sessionCounter = new AtomicLong(-1);

    /**
     * Computed UUID MSB value.
     */
    final long msb;

    /**
     * Counter for the UUID LSB.
     */
    final AtomicLong counter = new AtomicLong(-1);


    /**
     * Defines a new classloaderId for the class.
     * This only affects instances created after the call (the
     * instances created before the call keep their classloaderId).
     *
     * This method should be called to specify a classloaderId if
     * you are using more than one class loader, and you want to
     * guarantee a collision-free UUID generation (instead of
     * the default random classloaderId which produces about
     * 1% collision rate on the classloaderId, and thus can have
     * UUID collision if the epoch-time, session counter and
     * counter have the same values).
     * @param classloaderId the new classloaderId (only the least significant 12 bits are used)
     * @see IncrementingUuidGenerator#classloaderId
     */
    public static void setClassloaderId(int classloaderId) {
        IncrementingUuidGenerator.classloaderId = classloaderId & 0xfff;
    }

    public IncrementingUuidGenerator() {
        long sessionId = sessionCounter.incrementAndGet();
        if (sessionId == MAX_SESSION_ID) {
            throw new CucumberException(
                "Out of " + IncrementingUuidGenerator.class.getSimpleName() +
                        " capacity. Please reuse existing instances or use another " +
                        UuidGenerator.class.getSimpleName() + " implementation instead.");
        }
        long epochTime = System.currentTimeMillis();
        // msb = epochTime | sessionId | version | classloaderId
        msb = ((epochTime & MAX_EPOCH_TIME) << 24) | (sessionId << 16) | (8 << 12) | classloaderId;
    }

    /**
     * Generate a new UUID. Will throw an exception when out of capacity.
     *
     * @return                   a non-null UUID
     * @throws CucumberException when out of capacity
     */
    @Override
    public UUID generateId() {
        long counterValue = counter.incrementAndGet();
        if (counterValue == MAX_COUNTER_VALUE) {
            throw new CucumberException(
                "Out of " + IncrementingUuidGenerator.class.getSimpleName() +
                        " capacity. Please generate using a new instance or use another " +
                        UuidGenerator.class.getSimpleName() + "implementation.");
        }
        long leastSigBits = counterValue | 0x8000000000000000L; // set variant
        return new UUID(msb, leastSigBits);
    }
}
