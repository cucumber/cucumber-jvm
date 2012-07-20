package cucumber.runtime;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Timeout {
    public static <T> T timeout(Callback<T> callback, int timeoutMillis) throws Throwable {
        if (timeoutMillis == 0) {
            return callback.call();
        } else {
            final Thread executionThread = Thread.currentThread();
            final AtomicBoolean done = new AtomicBoolean();
            ScheduledFuture<?> timer = Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                @Override
                public void run() {
                    if (!done.get()) {
                        executionThread.interrupt();
                    }
                }
            }, timeoutMillis, TimeUnit.MILLISECONDS);
            try {
                T result = callback.call();
                timer.cancel(true);
                return result;
            } catch (InterruptedException timeout) {
                throw new TimeoutException("Timed out after " + timeoutMillis + "ms.");
            }
        }
    }

    public interface Callback<T> {
        T call() throws Throwable;
    }
}
