package cucumber.runtime.java.hk2.impl;

import cucumber.runtime.java.hk2.ScenarioScoped;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.ServiceHandle;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;

@Singleton
public class ScenarioContext implements Context<ScenarioScoped> {

    private boolean isActive = true;

    private HashMap<ActiveDescriptor<?>, Object> map = new HashMap<ActiveDescriptor<?>, Object>();

    @Override
    public Class<? extends Annotation> getScope() {
        return ScenarioScoped.class;
    }

    @Override
    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor, ServiceHandle<?> root) {

        Object val = map.get(activeDescriptor);

        if (val == null) {
            val = activeDescriptor.create(root);
            map.put(activeDescriptor, val);
        }

        return (U) val;
    }

    @Override
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        return map.containsKey(descriptor);
    }

    @Override
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        remove(descriptor);
    }

    @SuppressWarnings("unchecked")
    private <T> void remove(ActiveDescriptor<T> descriptor) {
        final T removed = (T) map.remove(descriptor);
        if (removed != null) {
            descriptor.dispose(removed);
        }
    }

    @Override
    public boolean supportsNullCreation() {
        return false;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void shutdown() {
        isActive = false;
        clear();
    }

    public void clear() {
        HashSet<ActiveDescriptor<?>> copyOfKeys = new HashSet<ActiveDescriptor<?>>(map.keySet());
        for (ActiveDescriptor<?> descriptor : copyOfKeys) {
            remove(descriptor);
        }
    }
}
