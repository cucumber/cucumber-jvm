package io.cucumber.guice;

import com.google.inject.Guice;
import com.google.inject.Stage;

import static java.text.MessageFormat.format;

final class InjectorSourceFactory {

    private final Class<?> injectorSourceClass;

    InjectorSourceFactory(Class<?> injectorSourceClass) {
        this.injectorSourceClass = injectorSourceClass;
    }

    InjectorSource create() {
        if (injectorSourceClass == null) {
            return createDefaultScenarioModuleInjectorSource();
        } else {
            return instantiateUserSpecifiedInjectorSource(injectorSourceClass);
        }
    }

    private InjectorSource createDefaultScenarioModuleInjectorSource() {
        return () -> Guice.createInjector(Stage.PRODUCTION, CucumberModules.createScenarioModule());
    }

    private InjectorSource instantiateUserSpecifiedInjectorSource(Class<?> injectorSourceClass) {
        try {
            return (InjectorSource) injectorSourceClass.getConstructor().newInstance();
        } catch (Exception e) {
            String message = format("Instantiation of ''{0}'' failed. Check the caused by exception and ensure your" +
                    "InjectorSource implementation is accessible and has a public zero args constructor.",
                injectorSourceClass.getName());
            throw new InjectorSourceInstantiationFailed(message, e);
        }
    }

}
