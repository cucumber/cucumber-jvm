package cucumber.runner;

public interface TimeService {
    long time();

    TimeService SYSTEM = new TimeService() {
        @Override
        public long time() {
            return System.nanoTime();
        }
    };

}
