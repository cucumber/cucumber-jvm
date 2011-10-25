package cucumber.runtime.java.guice;

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import cucumber.runtime.java.ObjectFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.util.Collections.emptyList;

public class GuiceFactory implements ObjectFactory {
    private final List<Module> modules;
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private final Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();

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
        InputStream inputStream = GuiceFactory.class.getClassLoader().getResourceAsStream("cucumber-guice.properties");
        if (inputStream != null) {
            properties.load(inputStream);
        }
        return properties;
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public void createInstances() {
        Injector injector = Guice.createInjector(modules);
        for (Class<?> clazz : classes) {
            try {
                instances.put(clazz, injector.getInstance(clazz));
            } catch (ConfigurationException e) {
                System.err.println("WARNING: Cucumber/Guice could not create instance for " + clazz.getCanonicalName() + ":\n" + e.getLocalizedMessage());
            }
        }
    }

    public void disposeInstances() {
        instances.clear();
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        return (T) instances.get(clazz);
    }
}
