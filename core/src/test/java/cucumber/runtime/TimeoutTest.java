package cucumber.runtime;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TimeoutTest {
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

    @Test(expected = TimeoutException.class)
    public void times_out_if_it_takes_too_long() throws Throwable {
        final Slow slow = new Slow();
        Timeout.timeout(new Timeout.Callback<String>() {
            @Override
            public String call() throws Throwable {
                return slow.slow(100);
            }
        }, 50);
        fail();
    }

    @Test(expected = TimeoutException.class)
    public void times_out_infinite_loop_if_it_takes_too_long() throws Throwable {
        final Slow slow = new Slow();
        Timeout.timeout(new Timeout.Callback<Void>() {
            @Override
            public Void call() throws Throwable {
                slow.infinite();
                return null;
            }
        }, 10);
        fail();
    }

    @Test(expected = TimeoutException.class)
    public void times_out_infinite_latch_wait_if_it_takes_too_long() throws Throwable {
        final Slow slow = new Slow();
        Timeout.timeout(new Timeout.Callback<Void>() {
            @Override
            public Void call() throws Throwable {
                slow.infiniteLatchWait();
                return null;
            }
        }, 10);
        fail();
    }


    @Test(expected = TimeoutException.class)
    public void times_out_busy_wait_if_it_takes_too_long() throws Throwable {
        final Slow slow = new Slow();
        Timeout.timeout(new Timeout.Callback<Void>() {
            @Override
            public Void call() throws Throwable {
                slow.busyWait();
                return null;
            }
        }, 1);
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
        int busyCounter = Integer.MIN_VALUE;

        public String slow(int millis) throws InterruptedException {
            sleep(millis);
            return String.format("slept %sms", millis);
        }

        public void infinite() throws InterruptedException {
            while (true) {
                sleep(1);
            }
        }

        public void infiniteLatchWait() throws InterruptedException {
            new CountDownLatch(1).await();
        }

        public int busyWait() throws InterruptedException {
            while (busyCounter < Integer.MAX_VALUE) {
                busyCounter += 1;
            }

            return busyCounter;
        }
    }
}
