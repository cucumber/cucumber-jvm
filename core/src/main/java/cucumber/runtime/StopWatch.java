package cucumber.runtime;

public interface StopWatch {
    void start();

    /**
     * @return nanoseconds since start
     */
    long stop();

    StopWatch SYSTEM = new StopWatch() {
        private final ThreadLocal<Long> start = new ThreadLocal<Long>();

        @Override
        public void start() {
            start.set(System.nanoTime());
        }

        @Override
        public long stop() {
            Long duration = System.nanoTime() - start.get();
            start.set(null);
            return duration;
        }
    };

    public static class Stub implements StopWatch {
        private final long duration;

        public Stub(long duration) {
            this.duration = duration;
        }

        @Override
        public void start() {
        }

        @Override
        public long stop() {
            return duration;
        }
    }
}
