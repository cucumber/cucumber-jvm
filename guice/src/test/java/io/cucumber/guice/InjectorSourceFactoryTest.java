package io.cucumber.guice;

import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

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
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(null);
        assertThat(injectorSourceFactory.create(), is(instanceOf(InjectorSource.class)));
    }

    private InjectorSourceFactory createInjectorSourceFactory(Class<?> injectorClassName) {
        return new InjectorSourceFactory(injectorClassName);
    }

    @Test
    void instantiatesInjectorSourceByFullyQualifiedName() {
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(CustomInjectorSource.class);
        assertThat(injectorSourceFactory.create(), is(instanceOf(CustomInjectorSource.class)));
    }

    @Test
    void failsToInstantiateClassNotImplementingInjectorSource() {
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(String.class);

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
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(PrivateConstructor.class);

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
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(NoDefaultConstructor.class);

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
