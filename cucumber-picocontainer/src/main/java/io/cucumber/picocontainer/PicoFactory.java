package io.cucumber.picocontainer;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.ObjectFactory;
import org.apiguardian.api.API;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoException;
import org.picocontainer.behaviors.Cached;
import org.picocontainer.injectors.Provider;
import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.lifecycle.DefaultLifecycleState;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

@API(status = API.Status.STABLE)
public final class PicoFactory implements ObjectFactory {

    private final Set<Class<?>> classes = new HashSet<>();
    private MutablePicoContainer pico;

    private static boolean isInstantiable(Class<?> clazz) {
        boolean isNonStaticInnerClass = !Modifier.isStatic(clazz.getModifiers()) && clazz.getEnclosingClass() != null;
        return Modifier.isPublic(clazz.getModifiers()) && !Modifier.isAbstract(clazz.getModifiers())
                && !isNonStaticInnerClass;
    }

    @Override
    public void start() {
        if (pico == null) {
            pico = new PicoBuilder()
                    .withCaching()
                    .withLifecycle()
                    .build();
            Set<Class<?>> providers = new HashSet<>();
            Set<Class<?>> providedClasses = new HashSet<>();
            for (Class<?> clazz : classes) {
                if (isProvider(clazz)) {
                    providers.add(clazz);
                    ProviderAdapter adapter = adapterForProviderClass(clazz);
                    pico.addAdapter(adapter);
                    providedClasses.add(adapter.getComponentImplementation());
                }
            }
            for (Class<?> clazz : classes) {
                // do not add the classes that represent a picocontainer
                // Provider, and also do not add those raw classes that are
                // already provided (otherwise this causes exceptional
                // situations, e.g. PicoCompositionException with message
                // "Duplicate Keys not allowed. Duplicate for 'class XXX'")
                if (!providers.contains(clazz) && !providedClasses.contains(clazz)) {
                    pico.addComponent(clazz);
                }
            }
        } else {
            // we already get a pico container which is in "disposed" lifecycle,
            // so recycle it by defining a new lifecycle and removing all
            // instances
            pico.setLifecycleState(new DefaultLifecycleState());
            pico.getComponentAdapters().forEach(adapters -> {
                if (adapters instanceof Cached) {
                    ((Cached<?>) adapters).flush();
                }
            });
        }
        pico.start();
    }

    static boolean isProvider(Class<?> clazz) {
        return Provider.class.isAssignableFrom(clazz);
    }

    static boolean isProviderAdapter(Class<?> clazz) {
        return ProviderAdapter.class.isAssignableFrom(clazz);
    }

    private static ProviderAdapter adapterForProviderClass(Class<?> clazz) {
        try {
            Provider provider = (Provider) clazz.getDeclaredConstructor().newInstance();
            return isProviderAdapter(clazz) ? (ProviderAdapter) provider : new ProviderAdapter(provider);
        } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException | PicoException e) {
            throw new CucumberBackendException(e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        if (pico.getLifecycleState().isStarted()) {
            pico.stop();
        }
        pico.dispose();
    }

    @Override
    public boolean addClass(Class<?> clazz) {
        if (isInstantiable(clazz) && classes.add(clazz)) {
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
