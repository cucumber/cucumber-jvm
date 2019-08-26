package cucumber.runtime.java.guice.impl;

import com.google.inject.Injector;
import cucumber.runtime.Env;
import cucumber.runtime.java.guice.InjectorSource;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class InjectorSourceFactoryTest {

    private InjectorSourceFactory createInjectorSourceFactory(Properties properties) {
        return new InjectorSourceFactory(new Env(properties));
    }

    @Test
    public void createsDefaultInjectorSourceWhenGuiceModulePropertyIsNotSet() throws Exception {
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(new Properties());
        assertThat(injectorSourceFactory.create(), is(instanceOf(InjectorSource.class)));
    }

    static class CustomInjectorSource implements InjectorSource {
        @Override
        public Injector getInjector() {
            return null;
        }
    }

    @Test
    public void instantiatesInjectorSourceByFullyQualifiedName() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY, CustomInjectorSource.class.getName());
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(properties);
        assertThat(injectorSourceFactory.create(), is(instanceOf(CustomInjectorSource.class)));
    }

    @Test
    public void failsToInstantiateNonExistantClass() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY, "some.bogus.Class");
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(properties);
        try {
            injectorSourceFactory.create();
            fail();
        } catch (InjectorSourceInstantiationFailed exception) {
            assertThat(exception.getCause(), instanceOf(ClassNotFoundException.class));
        }
    }

    @Test
    public void failsToInstantiateClassNotImplementingInjectorSource() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY, String.class.getName());
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(properties);
        try {
            injectorSourceFactory.create();
            fail();
        } catch (InjectorSourceInstantiationFailed exception) {
            assertThat(exception.getCause(), instanceOf(ClassCastException.class));
        }
    }

    static class PrivateConstructor implements InjectorSource {
        private PrivateConstructor() {
        }

        @Override
        public Injector getInjector() {
            return null;
        }
    }

    @Test
    public void failsToInstantiateClassWithPrivateConstructor() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY, PrivateConstructor.class.getName());
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(properties);
        try {
            injectorSourceFactory.create();
            fail();
        } catch (InjectorSourceInstantiationFailed exception) {
            assertThat(exception.getCause(), instanceOf(IllegalAccessException.class));
        }
    }

    static class NoDefaultConstructor implements InjectorSource {
        private NoDefaultConstructor(String someParameter) {
        }

        @Override
        public Injector getInjector() {
            return null;
        }
    }

    @Test
    public void failsToInstantiateClassWithNoDefaultConstructor() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY, NoDefaultConstructor.class.getName());
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(properties);
        try {
            injectorSourceFactory.create();
            fail();
        } catch (InjectorSourceInstantiationFailed exception) {
            assertThat(exception.getCause(), instanceOf(InstantiationException.class));
        }
    }
}