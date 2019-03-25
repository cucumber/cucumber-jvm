package cucumber.runner;

public interface TimeService {
    long time();
    long timeStampMillis();

    TimeService SYSTEM = new TimeService() {
        @Override
        public long time() {
            return System.nanoTime();
        }

        @Override
        public long timeStampMillis() {
            return System.currentTimeMillis();
        }
    };

}
