package io.cucumber.core.runner;

public interface TimeService {
    long time();
    long elapsedTimeMillis();

    TimeService SYSTEM = new TimeService() {
        @Override
        public long time() {
            return System.nanoTime();
        }

        @Override
        public long elapsedTimeMillis() {
            return System.currentTimeMillis();
        }
    };

}
