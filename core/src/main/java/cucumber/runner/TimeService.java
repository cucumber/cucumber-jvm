package cucumber.runner;

public interface TimeService {
    long time();

    TimeService SYSTEM = new TimeService() {
        @Override
        public long time() {
            return System.nanoTime();
        }
    };

    public static class Stub implements TimeService {
        private final long duration;
        private final ThreadLocal<Long> currentTime = new ThreadLocal<Long>();

        public Stub(long duration) {
            this.duration = duration;
        }

        @Override
        public long time() {
            Long result = currentTime.get();
            result = result != null ? result : 0l;
            currentTime.set(result + duration);
            return result;
        }
    }
}
