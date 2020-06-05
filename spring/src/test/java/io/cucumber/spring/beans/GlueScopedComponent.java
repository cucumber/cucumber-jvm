package io.cucumber.spring.beans;

import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@ScenarioScope
public class GlueScopedComponent {

    private static final AtomicLong counter = new AtomicLong(0);

    private final long instanceId = counter.incrementAndGet();

    @Autowired
    private Belly belly;

    public Belly getBelly() {
        return belly;
    }

    public long getInstanceId() {
        return instanceId;
    }

}
