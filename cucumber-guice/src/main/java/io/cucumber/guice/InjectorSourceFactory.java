package io.cucumber.guice;

import com.google.inject.Guice;
import com.google.inject.Stage;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.jspecify.annotations.Nullable;

import java.util.Map;

final class InjectorSourceFactory {
    private static final Logger log = LoggerFactory.getLogger(GuiceFactory.class);
    static final String GUICE_INJECTOR_SOURCE_KEY = "guice.injector-source";

    private InjectorSourceFactory() {
        /* no-op */
    }

    static InjectorSource createDefaultScenarioModuleInjectorSource() {
        return () -> Guice.createInjector(Stage.PRODUCTION, CucumberModules.createScenarioModule());
    }

    static InjectorSource instantiateUserSpecifiedInjectorSource(Class<?> injectorSourceClass) {
        try {
            return (InjectorSource) injectorSourceClass.getConstructor().newInstance();
        } catch (Exception e) {
            String message = ("Instantiation of '%s' failed. Check the caused by exception and ensure your " +
                    "InjectorSource implementation is accessible and has a public zero args constructor.")
                    .formatted(injectorSourceClass.getName());
            throw new InjectorSourceInstantiationFailed(message, e);
        }
    }

    @Deprecated
    static @Nullable Class<?> loadInjectorSourceFromProperties(Map<String, String> properties) {
        String injectorSourceClassName = properties.get(GUICE_INJECTOR_SOURCE_KEY);

        if (injectorSourceClassName == null) {
            return null;
        }

        log.warn(
            () -> ("The '%s' property has been deprecated." +
                    "Add a class implementing '%s' on the glue path instead")
                    .formatted(GUICE_INJECTOR_SOURCE_KEY, InjectorSource.class.getName()));

        try {
            return Class.forName(injectorSourceClassName, true, Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            String message = ("Instantiation of '%s' failed. Check the caused by exception and ensure your " +
                    "InjectorSource implementation is accessible and has a public zero args constructor.")
                    .formatted(injectorSourceClassName);
            throw new InjectorSourceInstantiationFailed(message, e);
        }
    }

}
