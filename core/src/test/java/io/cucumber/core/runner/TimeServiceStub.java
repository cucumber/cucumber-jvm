package io.cucumber.core.runner;

import java.time.Duration;
import java.time.Instant;

public class TimeServiceStub implements TimeService {
//    private final long durationMillis;
    private final Duration duration;
    private final ThreadLocal<Instant> currentInstant = new ThreadLocal<>();
//    private final ThreadLocal<Long> currentTime = new ThreadLocal<>();
//    private final ThreadLocal<Long> currentTimeMillis = new ThreadLocal<>();

    //gazler
//    @Deprecated
//    public TimeServiceStub(long durationMillis) {
//        this.durationMillis = durationMillis;
//        this.duration = Duration.ZERO;
//    }
//    
//    public TimeServiceStub(Duration duration) {
//        this.durationMillis = 0L;
//        this.duration = duration;
//    }
    
    public TimeServiceStub(Duration duration) {
      this.duration = duration;
  }

//    @Override
//    public long time() {
//        Long result = currentTime.get();
//        result = result != null ? result : 0L;
//        currentTime.set(result + durationMillis);
//        return MILLISECONDS.toNanos(result);
//    }
//
//    @Override
//    public long timeMillis() {
//        Long result = currentTimeMillis.get();
//        result = result != null ? result : 0L;
//        currentTimeMillis.set(result + durationMillis);
//        return result;
//    }
    
    @Override
    public Instant timeInstant() {
        Instant result = currentInstant.get();
        result = result != null ? result : Instant.EPOCH;
        currentInstant.set(result.plus(duration));
        return result;
    }
}
