package io.cucumber.guice;

import com.google.inject.Guice;
import com.google.inject.Stage;

import java.util.Map;

import static java.text.MessageFormat.format;

final class InjectorSourceFactory {

    static final String GUICE_INJECTOR_SOURCE_KEY = "guice.injector-source";
    private final Map<String, String> properties;

    InjectorSourceFactory(Map<String, String> properties) {
        this.properties = properties;
    }

    InjectorSource create() {
        String injectorSourceClassName = properties.get(GUICE_INJECTOR_SOURCE_KEY);
        if (injectorSourceClassName == null) {
            return createDefaultScenarioModuleInjectorSource();
        } else {
            return instantiateUserSpecifiedInjectorSource(injectorSourceClassName);
        }
    }

    private InjectorSource createDefaultScenarioModuleInjectorSource() {
        return () -> Guice.createInjector(Stage.PRODUCTION, CucumberModules.createScenarioModule());
    }

    private InjectorSource instantiateUserSpecifiedInjectorSource(String injectorSourceClassName) {
        try {
            return (InjectorSource) Class
                    .forName(injectorSourceClassName, true, Thread.currentThread().getContextClassLoader())
                    .newInstance();
        } catch (Exception e) {
            String message = format("Instantiation of ''{0}'' failed. Check the caused by exception and ensure your" +
                    "InjectorSource implementation is accessible and has a public zero args constructor.",
                injectorSourceClassName);
            throw new InjectorSourceInstantiationFailed(message, e);
        }
    }

}
