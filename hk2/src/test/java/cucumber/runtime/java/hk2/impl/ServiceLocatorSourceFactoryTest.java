package cucumber.runtime.java.hk2.impl;

import cucumber.runtime.Env;
import cucumber.runtime.java.hk2.ServiceLocatorSource;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ServiceLocatorSourceFactoryTest {

    private ServiceLocatorSourceFactory createServiceLocatorSourceFactory(Properties properties) {
        return new ServiceLocatorSourceFactory(new Env(properties));
    }

    @Test
    public void createsDefaultServiceLocatorSourceWhenHk2ModulePropertyIsNotSet() throws Exception {
        ServiceLocatorSourceFactory injectorSourceFactory = createServiceLocatorSourceFactory(new Properties());
        assertThat(injectorSourceFactory.create(), is(instanceOf(ServiceLocatorSource.class)));
    }

    static class CustomServiceLocatorSource implements ServiceLocatorSource {
        @Override
        public ServiceLocator getServiceLocator() {
            return null;
        }
    }

    @Test
    public void instantiatesServiceLocatorSourceByFullyQualifiedName() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(ServiceLocatorSourceFactory.HK2_LOCATOR_SOURCE_KEY, CustomServiceLocatorSource.class.getName());
        ServiceLocatorSourceFactory injectorSourceFactory = createServiceLocatorSourceFactory(properties);
        assertThat(injectorSourceFactory.create(), is(instanceOf(CustomServiceLocatorSource.class)));
    }

    @Test
    public void failsToInstantiateNonExistantClass() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(ServiceLocatorSourceFactory.HK2_LOCATOR_SOURCE_KEY, "some.bogus.Class");
        ServiceLocatorSourceFactory injectorSourceFactory = createServiceLocatorSourceFactory(properties);
        try {
            injectorSourceFactory.create();
            fail();
        } catch (CucumberHK2Exception exception) {
            assertThat(exception.getCause(), instanceOf(ClassNotFoundException.class));
        }
    }

    @Test
    public void failsToInstantiateClassNotImplementingServiceLocatorSource() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(ServiceLocatorSourceFactory.HK2_LOCATOR_SOURCE_KEY, String.class.getName());
        ServiceLocatorSourceFactory injectorSourceFactory = createServiceLocatorSourceFactory(properties);
        try {
            injectorSourceFactory.create();
            fail();
        } catch (CucumberHK2Exception exception) {
            assertThat(exception.getCause(), instanceOf(ClassCastException.class));
        }
    }

    static class PrivateConstructor implements ServiceLocatorSource {
        private PrivateConstructor() {
        }

        @Override
        public ServiceLocator getServiceLocator() {
            return null;
        }
    }

    @Test
    public void failsToInstantiateClassWithPrivateConstructor() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(ServiceLocatorSourceFactory.HK2_LOCATOR_SOURCE_KEY, PrivateConstructor.class.getName());
        ServiceLocatorSourceFactory injectorSourceFactory = createServiceLocatorSourceFactory(properties);
        try {
            injectorSourceFactory.create();
            fail();
        } catch (CucumberHK2Exception exception) {
            assertThat(exception.getCause(), instanceOf(IllegalAccessException.class));
        }
    }

    static class NoDefaultConstructor implements ServiceLocatorSource {
        private NoDefaultConstructor(String someParameter) {
        }

        @Override
        public ServiceLocator getServiceLocator() {
            return null;
        }
    }

    @Test
    public void failsToInstantiateClassWithNoDefaultConstructor() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(ServiceLocatorSourceFactory.HK2_LOCATOR_SOURCE_KEY, NoDefaultConstructor.class.getName());
        ServiceLocatorSourceFactory injectorSourceFactory = createServiceLocatorSourceFactory(properties);
        try {
            injectorSourceFactory.create();
            fail();
        } catch (CucumberHK2Exception exception) {
            assertThat(exception.getCause(), instanceOf(InstantiationException.class));
        }
    }
}