package io.cucumber.spring;

import org.apiguardian.api.API;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@API(status = API.Status.STABLE)
public final class CucumberTestContext {

    public static final String SCOPE_CUCUMBER_GLUE = "cucumber-glue";

    private static final ThreadLocal<CucumberTestContext> localContext = ThreadLocal
            .withInitial(CucumberTestContext::new);
    private static final AtomicInteger sessionCounter = new AtomicInteger(0);

    private final Map<String, Object> objects = new HashMap<>();
    private final Map<String, Runnable> callbacks = new HashMap<>();

    private Integer sessionId;

    private CucumberTestContext() {
    }

    static CucumberTestContext getInstance() {
        return localContext.get();
    }

    void start() {
        sessionId = sessionCounter.incrementAndGet();
    }

    Optional<Integer> getId() {
        return Optional.ofNullable(sessionId);
    }

    void stop() {
        for (Runnable callback : callbacks.values()) {
            callback.run();
        }
        localContext.remove();
        sessionId = null;
    }

    Object get(String name) {
        requireActiveScenario();
        return objects.get(name);
    }

    void put(String name, Object object) {
        requireActiveScenario();
        objects.put(name, object);
    }

    Object remove(String name) {
        requireActiveScenario();
        callbacks.remove(name);
        return objects.remove(name);
    }

    void registerDestructionCallback(String name, Runnable callback) {
        requireActiveScenario();
        callbacks.put(name, callback);
    }

    void requireActiveScenario() {
        if (!isActive()) {
            throw new IllegalStateException(
                "Scenario scoped beans can only be created while Cucumber is executing a scenario");
        }
    }

    boolean isActive() {
        return sessionId != null;
    }

}
