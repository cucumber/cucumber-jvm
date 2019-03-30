package cucumber.runner;

public interface TimeService {
    long time();
    long timeMillis();

    TimeService SYSTEM = new TimeService() {
        @Override
        public long time() {
            return System.nanoTime();
        }

        @Override
        public long timeMillis() {
            return System.currentTimeMillis();
        }
    };

}
