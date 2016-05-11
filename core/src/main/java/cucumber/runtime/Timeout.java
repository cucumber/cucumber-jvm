package cucumber.runtime;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Timeout {
    private Timeout() {
    }

    public static <T> T timeout(Callback<T> callback, long timeoutMillis) throws Throwable {
        if (timeoutMillis == 0) {
            return callback.call();
        } else {
            final Thread executionThread = Thread.currentThread();
            final AtomicBoolean done = new AtomicBoolean();

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            ScheduledFuture<?> timer = executorService.schedule(new Runnable() {
                @Override
                public void run() {
                    if (!done.get()) {
                        executionThread.interrupt();
                    }
                }
            }, timeoutMillis, TimeUnit.MILLISECONDS);
            try {
                return callback.call();
            } catch (InterruptedException timeout) {
                throw new TimeoutException("Timed out after " + timeoutMillis + "ms.");
            } finally {
                done.set(true);
                timer.cancel(true);
                executorService.shutdownNow();
            }

        }
    }

    public interface Callback<T> {
        T call() throws Throwable;
    }
}
