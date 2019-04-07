package io.cucumber.core.runner;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TimeServiceStub implements TimeService {
    private final long durationMillis;
    private final ThreadLocal<Long> currentTime = new ThreadLocal<>();
    private final ThreadLocal<Long> currentTimeMillis = new ThreadLocal<>();

    public TimeServiceStub(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    @Override
    public long time() {
        Long result = currentTime.get();
        result = result != null ? result : 0L;
        currentTime.set(result + durationMillis);
        return MILLISECONDS.toNanos(result);
    }

    @Override
    public long timeMillis() {
        Long result = currentTimeMillis.get();
        result = result != null ? result : 0L;
        currentTimeMillis.set(result + durationMillis);
        return result;
    }
}
