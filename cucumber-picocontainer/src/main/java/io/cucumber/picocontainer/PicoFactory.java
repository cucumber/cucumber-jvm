package io.cucumber.picocontainer;

import io.cucumber.core.backend.ObjectFactory;
import org.apiguardian.api.API;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.behaviors.Cached;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

@API(status = API.Status.STABLE)
public final class PicoFactory implements ObjectFactory {

    private final Set<Class<?>> classes = new HashSet<>();
    private final MutablePicoContainer pico = new PicoBuilder()
            .withCaching()
            .withLifecycle()
            .build();

    public PicoFactory() {
        this.pico.start();
    }

    private static boolean isInstantiable(Class<?> clazz) {
        boolean isNonStaticInnerClass = !Modifier.isStatic(clazz.getModifiers()) && clazz.getEnclosingClass() != null;
        return Modifier.isPublic(clazz.getModifiers()) && !Modifier.isAbstract(clazz.getModifiers())
                && !isNonStaticInnerClass;
    }

    public void start() {
        // do nothing (was already started in constructor)
    }

    @Override
    public void stop() {
        pico.getComponentAdapters()
                .forEach(cached -> ((Cached<?>) cached).flush());
    }

    @Override
    public boolean addClass(Class<?> clazz) {
        if (isInstantiable(clazz) && classes.add(clazz)) {
            pico.addComponent(clazz);
            addConstructorDependencies(clazz);
        }
        return true;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return pico.getComponent(type);
    }

    private void addConstructorDependencies(Class<?> clazz) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            for (Class<?> paramClazz : constructor.getParameterTypes()) {
                addClass(paramClazz);
            }
        }
    }

}
