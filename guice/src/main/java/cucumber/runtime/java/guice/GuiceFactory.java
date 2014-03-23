package cucumber.runtime.java.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import cucumber.runtime.java.ObjectFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static java.util.Collections.emptyList;

/**
 * Guice implementation of the ObjectFactory. This will register all given step classes as singletons within a scenario
 * specific Injector.
 */
public class GuiceFactory implements ObjectFactory {
    private final List<Module> modules;
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private Injector injector;

    public GuiceFactory() throws IOException {
        this(loadCucumberGuiceProperties());
    }

    GuiceFactory(Properties properties) throws IOException {
        String guiceModuleClassName = properties.getProperty("guiceModule");
        if (guiceModuleClassName == null) {
            this.modules = emptyList();
        } else {
            this.modules = new ModuleInstantiator().instantiate(guiceModuleClassName);
        }
    }

    private static Properties loadCucumberGuiceProperties() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = GuiceFactory.class.getResourceAsStream("/cucumber-guice.properties");
        if (inputStream != null) {
            try {
                properties.load(inputStream);
            } finally {
                inputStream.close();
            }
        }
        return properties;
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public void start() {
        injector = createInjector(new CucumberModule(classes, modules));
    }

    /**
     * Create the injector. May be overriden to e.g. create an injector with a parent
     */
    protected Injector createInjector(Module cucumberModule) {
        return Guice.createInjector(cucumberModule);
    }

    public void stop() {
        injector = null;
    }

    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    /**
     * Guice module that configures all added classes to the module as singletons and installs all dynamically loaded
     * modules.
     */
    private static final class CucumberModule extends AbstractModule {

        private final Set<Class<?>> classes;
        private final List<Module> modules;

        private CucumberModule(Set<Class<?>> classes, List<Module> modules) {
            this.classes = classes;
            this.modules = modules;
        }

        @Override
        protected void configure() {
            for (Class<?> aClass : classes) {
                bind(aClass).in(javax.inject.Singleton.class);
            }
            for (Module module : modules) {
                install(module);
            }
        }
    }
}
