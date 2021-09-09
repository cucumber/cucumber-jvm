package io.cucumber.spring.beans;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class Belly {

    private static final AtomicLong counter = new AtomicLong(0);

    private final long instanceId = counter.incrementAndGet();

    private int cukes = 0;

    public int getCukes() {
        return cukes;
    }

    public void setCukes(int cukes) {
        this.cukes = cukes;
    }

    public long getInstanceId() {
        return instanceId;
    }

}
