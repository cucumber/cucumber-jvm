package cucumber.runtime;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TimeoutTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void doesnt_time_out_if_it_doesnt_take_too_long() throws Throwable {
        final Slow slow = new Slow();
        String what = Timeout.timeout(new Timeout.Callback<String>() {
            @Override
            public String call() throws Throwable {
                return slow.slow(10);
            }
        }, 50);
        assertEquals("slept 10ms", what);
    }

    @Test
    public void times_out_if_it_takes_too_long() throws Throwable {
        try {
            final Slow slow = new Slow();
            Timeout.timeout(new Timeout.Callback<String>() {
                @Override
                public String call() throws Throwable {
                    return slow.slow(100);
                }
            }, 50);
            fail();
        } catch (TimeoutException expected) {
            assertEquals("Timed out after 50ms.", expected.getMessage());
        }
    }

    @Test
    public void times_out_infinite_spin_loop_if_it_takes_too_long() throws Throwable {
        try {
            final Slow slow = new Slow();
            Timeout.timeout(new Timeout.Callback<Void>() {
                @Override
                public Void call() throws Throwable {
                    slow.infiniteSpin();
                    return null;
                }
            }, 10);
            fail();
        } catch (TimeoutException expected) {
            assertEquals("Timed out after 10ms. (Stopped the thread was uninterruptible).", expected.getMessage());
        }
    }

    @Test
    public void times_out_infinite_latch_wait_if_it_takes_too_long() throws Throwable {
        try {
            final Slow slow = new Slow();
            Timeout.timeout(new Timeout.Callback<Void>() {
                @Override
                public Void call() throws Throwable {
                    slow.infiniteLatchWait();
                    return null;
                }
            }, 10);
            fail();
        } catch (TimeoutException expected) {
            assertEquals("Timed out after 10ms.", expected.getMessage());
        }
    }

    @Test
    public void doesnt_leak_threads() throws Throwable {

        long initialNumberOfThreads = Thread.getAllStackTraces().size();
        long currentNumberOfThreads = Long.MAX_VALUE;

        boolean cleanedUp = false;
        for (int i = 0; i < 1000; i++) {
            Timeout.timeout(new Timeout.Callback<String>() {
                @Override
                public String call() throws Throwable {
                    return null;
                }
            }, 10);
            Thread.sleep(5);
            currentNumberOfThreads = Thread.getAllStackTraces().size();
            if (i > 20 && currentNumberOfThreads <= initialNumberOfThreads) {
                cleanedUp = true;
                break;
            }
        }
        assertTrue(String.format("Threads weren't cleaned up, initial count: %d current count: %d",
                        initialNumberOfThreads, currentNumberOfThreads),
                cleanedUp);
    }

    public static class Slow {
        public String slow(int millis) throws InterruptedException {
            sleep(millis);
            return String.format("slept %sms", millis);
        }

        public void infiniteSpin() throws InterruptedException {
            while (true) {
            }
        }

        public void infiniteLatchWait() throws InterruptedException {
            new CountDownLatch(1).await();
        }
    }
}
