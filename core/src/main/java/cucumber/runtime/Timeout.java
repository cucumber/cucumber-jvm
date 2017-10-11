package cucumber.runtime;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Timeout {
    private Timeout() {
    }

    public static <T> T timeout(Callback<T> callback, long timeoutMillis) throws Throwable {
        if (timeoutMillis == 0) {
            return callback.call();
        }

        /* We need to ensure a happens before relation exists between these events;
         *   a. the timer setting the interrupt flag on the execution thread.
         *   b. terminating and cleaning up the timer
         * To do this we synchronize on monitor. The atomic boolean is merely a convenient container.
         */
        final Thread executionThread = Thread.currentThread();
        final Object monitor = new Object();
        final AtomicBoolean done = new AtomicBoolean();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> timer = executorService.schedule(new Runnable() {
            @Override
            public void run() {
                synchronized (monitor) {
                    if (!done.get()) {
                        executionThread.interrupt();
                    }
                }
            }
        }, timeoutMillis, TimeUnit.MILLISECONDS);

        try {
            T result = callback.call();
            // The callback may have been busy waiting.
            if (Thread.interrupted()) {
                throw new TimeoutException("Timed out after " + timeoutMillis + "ms.");
            }
            return result;
        } catch (InterruptedException timeout) {
            throw new TimeoutException("Timed out after " + timeoutMillis + "ms.");
        } finally {
            synchronized (monitor) {
                done.set(true);
                timer.cancel(true);
                executorService.shutdownNow();
                // Clear the interrupted flag. It may have been set by the timer just before we returned the result.
                Thread.interrupted();
            }
        }
    }

    public interface Callback<T> {
        T call() throws Throwable;
    }
}
