package cucumber.runner;

public class TimeServiceStub implements TimeService {
    private final long duration;
    private final ThreadLocal<Long> currentTime = new ThreadLocal<Long>();

    public TimeServiceStub(long duration) {
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
