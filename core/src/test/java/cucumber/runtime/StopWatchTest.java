package cucumber.runtime;

import static org.junit.Assert.assertNull;

import org.junit.Test;

public class StopWatchTest {
    private final StopWatch stopWatch = StopWatch.SYSTEM;
    private Throwable exception;

    @Test
    public void should_be_thread_safe() {
        try {
            Thread timerThreadOne = new TimerThread(500L);
            Thread timerThreadTwo = new TimerThread(750L);

            timerThreadOne.start();
            timerThreadTwo.start();

            timerThreadOne.join();
            timerThreadTwo.join();

            assertNull("null_pointer_exception", exception);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    class TimerThread extends Thread {
        private final long timeoutMillis;

        public TimerThread(long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
        }

        @Override
        public void run() {
            try {
                stopWatch.start();
                Thread.sleep(timeoutMillis);
                stopWatch.stop();
            } catch (NullPointerException e) {
                exception = e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
