package io.cucumber.guice;

import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.HashMap;
import java.util.Map;

import static io.cucumber.guice.InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InjectorSourceFactoryTest {

    @Test
    void createsDefaultInjectorSourceWhenGuiceModulePropertyIsNotSet() {
        InjectorSourceFactory injectorSourceFactory = new InjectorSourceFactory(null);
        assertThat(injectorSourceFactory.create(), is(instanceOf(InjectorSource.class)));
    }

    @Test
    void instantiatesInjectorSourceByFullyQualifiedName() {
        Map<String, String> properties = new HashMap<>();
        properties.put(GUICE_INJECTOR_SOURCE_KEY, CustomInjectorSource.class.getName());

        Class<?> aClass = InjectorSourceFactory.loadInjectorSourceFromProperties(properties);
        assertThat(aClass, is(CustomInjectorSource.class));
    }

    @Test
    void failsToLoadNonExistantClass() {
        Map<String, String> properties = new HashMap<>();
        properties.put(GUICE_INJECTOR_SOURCE_KEY, "some.bogus.Class");

        InjectorSourceInstantiationFailed actualThrown = assertThrows(InjectorSourceInstantiationFailed.class,
            () -> InjectorSourceFactory.loadInjectorSourceFromProperties(properties));
        assertAll(
            () -> assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
                "Instantiation of 'some.bogus.Class' failed. Check the caused by exception and ensure yourInjectorSource implementation is accessible and has a public zero args constructor."))),
            () -> assertThat("Unexpected exception cause class", actualThrown.getCause(),
                isA(ClassNotFoundException.class)));
    }

    @Test
    void failsToInstantiateClassNotImplementingInjectorSource() {
        InjectorSourceFactory injectorSourceFactory = new InjectorSourceFactory(String.class);

        Executable testMethod = injectorSourceFactory::create;
        InjectorSourceInstantiationFailed actualThrown = assertThrows(InjectorSourceInstantiationFailed.class,
            testMethod);
        assertAll(
            () -> assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
                "Instantiation of 'java.lang.String' failed. Check the caused by exception and ensure yourInjectorSource implementation is accessible and has a public zero args constructor."))),
            () -> assertThat("Unexpected exception cause class", actualThrown.getCause(),
                isA(ClassCastException.class)));
    }

    @Test
    void failsToInstantiateClassWithPrivateConstructor() {
        InjectorSourceFactory injectorSourceFactory = new InjectorSourceFactory(PrivateConstructor.class);

        Executable testMethod = injectorSourceFactory::create;
        InjectorSourceInstantiationFailed actualThrown = assertThrows(InjectorSourceInstantiationFailed.class,
            testMethod);
        assertAll(
            () -> assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
                "Instantiation of 'io.cucumber.guice.InjectorSourceFactoryTest$PrivateConstructor' failed. Check the caused by exception and ensure yourInjectorSource implementation is accessible and has a public zero args constructor."))),
            () -> assertThat("Unexpected exception cause class", actualThrown.getCause(),
                isA(NoSuchMethodException.class)));
    }

    @Test
    void failsToInstantiateClassWithNoDefaultConstructor() {
        InjectorSourceFactory injectorSourceFactory = new InjectorSourceFactory(NoDefaultConstructor.class);

        Executable testMethod = injectorSourceFactory::create;
        InjectorSourceInstantiationFailed actualThrown = assertThrows(InjectorSourceInstantiationFailed.class,
            testMethod);
        assertAll(
            () -> assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
                "Instantiation of 'io.cucumber.guice.InjectorSourceFactoryTest$NoDefaultConstructor' failed. Check the caused by exception and ensure yourInjectorSource implementation is accessible and has a public zero args constructor."))),
            () -> assertThat("Unexpected exception cause class", actualThrown.getCause(),
                isA(NoSuchMethodException.class)));
    }

    public static class CustomInjectorSource implements InjectorSource {

        @Override
        public Injector getInjector() {
            return null;
        }

    }

    public static class PrivateConstructor implements InjectorSource {

        private PrivateConstructor() {
        }

        @Override
        public Injector getInjector() {
            return null;
        }

    }

    public static class NoDefaultConstructor implements InjectorSource {

        private NoDefaultConstructor(String someParameter) {
        }

        @Override
        public Injector getInjector() {
            return null;
        }

    }

}
