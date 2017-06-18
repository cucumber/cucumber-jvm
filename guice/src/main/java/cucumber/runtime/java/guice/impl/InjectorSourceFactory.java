package cucumber.runtime.java.guice.impl;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import cucumber.runtime.Env;
import cucumber.runtime.java.guice.InjectorSource;

import static java.text.MessageFormat.format;

public class InjectorSourceFactory {

    public static final String GUICE_INJECTOR_SOURCE_KEY = "guice.injector-source";
    private final Env env;

    public InjectorSourceFactory(Env env) {
        this.env = env;
    }

    public InjectorSource create() {
        String injectorSourceClassName = env.get(GUICE_INJECTOR_SOURCE_KEY);
        if (injectorSourceClassName == null) {
            return createDefaultScenarioModuleInjectorSource();
        } else {
            return instantiateUserSpecifiedInjectorSource(injectorSourceClassName);
        }
    }

    private InjectorSource createDefaultScenarioModuleInjectorSource() {
        return new InjectorSource() {
            @Override
            public Injector getInjector() {
                ScenarioModule scenarioModule = new ScenarioModule(new SequentialScenarioScope());
                return Guice.createInjector(Stage.PRODUCTION, scenarioModule);
            }
        };
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