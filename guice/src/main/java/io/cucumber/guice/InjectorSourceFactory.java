package io.cucumber.guice;

import com.google.inject.Guice;
import com.google.inject.Stage;
import io.cucumber.core.options.Env;

import static java.text.MessageFormat.format;

final class InjectorSourceFactory {

    static final String GUICE_INJECTOR_SOURCE_KEY = "guice.injector-source";
    private final Env env;

    InjectorSourceFactory(Env env) {
        this.env = env;
    }

    InjectorSource create() {
        String injectorSourceClassName = env.get(GUICE_INJECTOR_SOURCE_KEY);
        if (injectorSourceClassName == null) {
            return createDefaultScenarioModuleInjectorSource();
        } else {
            return instantiateUserSpecifiedInjectorSource(injectorSourceClassName);
        }
    }

    private InjectorSource createDefaultScenarioModuleInjectorSource() {
        return () -> Guice.createInjector(Stage.PRODUCTION, CucumberModules.SCENARIO);
    }

    private InjectorSource instantiateUserSpecifiedInjectorSource(String injectorSourceClassName) {
        try {
            return (InjectorSource) Class.forName(injectorSourceClassName, true, Thread.currentThread().getContextClassLoader()).newInstance();
        } catch (Exception e) {
            String message = format("Instantiation of ''{0}'' failed. Check the caused by exception and ensure your" +
                    "InjectorSource implementation is accessible and has a public zero args constructor.",
                    injectorSourceClassName);
            throw new InjectorSourceInstantiationFailed(message, e);
        }
    }

}