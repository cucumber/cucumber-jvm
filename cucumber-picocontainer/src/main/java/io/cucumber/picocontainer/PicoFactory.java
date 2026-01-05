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
    private final Set<Class<Provider>> providers = new HashSet<>();
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
            Set<Class<?>> providedClasses = new HashSet<>();
            for (Class<Provider> clazz : providers) {
                ProviderAdapter adapter = adapterForProviderClass(clazz);
                pico.addAdapter(adapter);
                providedClasses.add(adapter.getComponentImplementation());
            }
            for (Class<?> clazz : classes) {
                // do not add classes that are already provided (otherwise this
                // causes exceptional situations, e.g. PicoCompositionException
                // with message "Duplicate Keys not allowed. Duplicate for
                // 'class XXX'")
                if (!providedClasses.contains(clazz)) {
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

    private static boolean isProvider(Class<?> clazz) {
        return Provider.class.isAssignableFrom(clazz);
    }

    private static boolean isProviderAdapter(Class<?> clazz) {
        return ProviderAdapter.class.isAssignableFrom(clazz);
    }

    private static ProviderAdapter adapterForProviderClass(Class<Provider> clazz) {
        try {
            Provider provider = clazz.getDeclaredConstructor().newInstance();
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
        if (isProvider(clazz)) {
            providers.add(requireConstructableProvider(clazz));
        } else {
            if (isInstantiable(clazz) && classes.add(clazz)) {
                addConstructorDependencies(clazz);
            }
        }
        return true;
    }

    private static boolean hasDefaultConstructor(Class<?> clazz) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static Class<Provider> requireConstructableProvider(Class<?> clazz) {
        if (!isProvider(clazz) || !isInstantiable(clazz) || !hasDefaultConstructor(clazz)) {
            throw new CucumberBackendException(String.format("" +
                    "Glue class %1$s was annotated with @CucumberPicoProvider; marking it as a candidate for declaring a"
                    +
                    "PicoContainer Provider instance. Please ensure that all of the following requirements are satisfied:\n"
                    +
                    "1) the class implements org.picocontainer.injectors.Provider\n" +
                    "2) the class is public\n" +
                    "3) the class is not abstract\n" +
                    "4) the class provides a default constructor\n" +
                    "5) if nested, the class is static.",
                clazz.getName()));
        }
        return (Class<Provider>) clazz;
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
