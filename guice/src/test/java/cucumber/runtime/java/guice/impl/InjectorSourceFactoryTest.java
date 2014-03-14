package cucumber.runtime.java.guice.impl;

import com.google.inject.Injector;
import cucumber.runtime.java.guice.InjectorSource;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class InjectorSourceFactoryTest {
    private Properties properties;
    private InjectorSourceFactory injectorSourceFactory;

    @Before
    public void setUp() {
        properties = new Properties();
        injectorSourceFactory = new InjectorSourceFactory(properties);
    }

    @Test
    public void createsDefaultInjectorSourceWhenGuiceModulePropertyIsNotSet() throws Exception {
        assertThat(injectorSourceFactory.create(), is(instanceOf(InjectorSource.class)));
    }

    static class CustomInjectorSource implements InjectorSource {
        @Override public Injector getInjector() { return null; }
    }

    @Test
    public void instantiatesInjectorSourceByFullyQualifiedName() throws Exception {
        properties.setProperty(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY, nameOf(CustomInjectorSource.class));
        assertThat(injectorSourceFactory.create(), is(instanceOf(CustomInjectorSource.class)));
    }

    @Test
    public void failsToInstantiateNonExistantClass() throws Exception {
        properties.setProperty(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY, "some.bogus.Class");
        try {
            injectorSourceFactory.create();
            fail();
        } catch (InjectorSourceInstantiationFailed exception) {
            assertThat(exception.getCause(), instanceOf(ClassNotFoundException.class));
        }
    }

    @Test
    public void failsToInstantiateClassNotImplementingInjectorSource() throws Exception {
        properties.setProperty(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY, nameOf(String.class));
        try {
            injectorSourceFactory.create();
            fail();
        } catch (InjectorSourceInstantiationFailed exception) {
            assertThat(exception.getCause(), instanceOf(ClassCastException.class));
        }
    }

    static class PrivateConstructor implements InjectorSource {
        private PrivateConstructor() {}
        @Override public Injector getInjector() { return null; }
    }

    @Test
    public void failsToInstantiateClassWithPrivateConstructor() throws Exception {
        properties.setProperty(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY, nameOf(PrivateConstructor.class));
        try {
            injectorSourceFactory.create();
            fail();
        } catch (InjectorSourceInstantiationFailed exception) {
            assertThat(exception.getCause(), instanceOf(IllegalAccessException.class));
        }
    }

    static class NoDefaultConstructor implements InjectorSource {
        private NoDefaultConstructor(String someParameter) {}
        @Override public Injector getInjector() { return null; }
    }

    @Test
    public void failsToInstantiateClassWithNoDefaultConstructor() throws Exception {
        properties.setProperty(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY, nameOf(NoDefaultConstructor.class));
        try {
            injectorSourceFactory.create();
            fail();
        } catch (InjectorSourceInstantiationFailed exception) {
            assertThat(exception.getCause(), instanceOf(InstantiationException.class));
        }
    }

    private String nameOf(Class<?> moduleClass) {
        return moduleClass.getName();
    }


}