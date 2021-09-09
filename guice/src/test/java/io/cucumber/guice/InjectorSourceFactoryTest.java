package io.cucumber.guice;

import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(Collections.emptyMap());
        assertThat(injectorSourceFactory.create(), is(instanceOf(InjectorSource.class)));
    }

    private InjectorSourceFactory createInjectorSourceFactory(Map<String, String> properties) {
        return new InjectorSourceFactory(properties);
    }

    @Test
    void instantiatesInjectorSourceByFullyQualifiedName() {
        Map<String, String> properties = new HashMap<>();
        properties.put(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY, CustomInjectorSource.class.getName());
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(properties);
        assertThat(injectorSourceFactory.create(), is(instanceOf(CustomInjectorSource.class)));
    }

    @Test
    void failsToInstantiateNonExistantClass() {
        Map<String, String> properties = new HashMap<>();
        properties.put(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY, "some.bogus.Class");
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(properties);

        Executable testMethod = injectorSourceFactory::create;
        InjectorSourceInstantiationFailed actualThrown = assertThrows(InjectorSourceInstantiationFailed.class,
            testMethod);
        assertAll(
            () -> assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
                "Instantiation of 'some.bogus.Class' failed. Check the caused by exception and ensure yourInjectorSource implementation is accessible and has a public zero args constructor."))),
            () -> assertThat("Unexpected exception cause class", actualThrown.getCause(),
                isA(ClassNotFoundException.class)));
    }

    @Test
    void failsToInstantiateClassNotImplementingInjectorSource() {
        Map<String, String> properties = new HashMap<>();
        properties.put(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY, String.class.getName());
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(properties);

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
        Map<String, String> properties = new HashMap<>();
        properties.put(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY, PrivateConstructor.class.getName());
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(properties);

        Executable testMethod = injectorSourceFactory::create;
        InjectorSourceInstantiationFailed actualThrown = assertThrows(InjectorSourceInstantiationFailed.class,
            testMethod);
        assertAll(
            () -> assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
                "Instantiation of 'io.cucumber.guice.InjectorSourceFactoryTest$PrivateConstructor' failed. Check the caused by exception and ensure yourInjectorSource implementation is accessible and has a public zero args constructor."))),
            () -> assertThat("Unexpected exception cause class", actualThrown.getCause(),
                isA(IllegalAccessException.class)));
    }

    @Test
    void failsToInstantiateClassWithNoDefaultConstructor() {
        Map<String, String> properties = new HashMap<>();
        properties.put(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY, NoDefaultConstructor.class.getName());
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(properties);

        Executable testMethod = injectorSourceFactory::create;
        InjectorSourceInstantiationFailed actualThrown = assertThrows(InjectorSourceInstantiationFailed.class,
            testMethod);
        assertAll(
            () -> assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
                "Instantiation of 'io.cucumber.guice.InjectorSourceFactoryTest$NoDefaultConstructor' failed. Check the caused by exception and ensure yourInjectorSource implementation is accessible and has a public zero args constructor."))),
            () -> assertThat("Unexpected exception cause class", actualThrown.getCause(),
                isA(InstantiationException.class)));
    }

    /**
     * <p>
     * Simulates enterprise applications which often use a hierarchy of
     * classloaders.
     * <p>
     * MyChildClassLoader is the only classloader with knowledge of
     * c.r.j.guice.impl.LivesInChildClassLoader
     * <p>
     * The bytecode of LivesInChildClassLoader is intentionally renamed to
     * 'LivesInChildClassLoader.class.bin.txt' to prevent this test's
     * ClassLoader from resolving it.
     * <p>
     * If InjectorSourceFactory calls Class#forName without an explicit
     * ClassLoader argument, which is the behavior of 1.2.4 and earlier,
     * Class#forName will default to the test's ClassLoader which has no
     * knowledge of class LivesInChildClassLoader and the test will fail.
     * <p>
     * 
     * @see https://github.com/cucumber/cucumber-jvm/issues/1036
     */
    @Test
    void instantiateClassInChildClassLoader() {
        ClassLoader childClassLoader = new MyChildClassLoader(this.getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(childClassLoader);

        Map<String, String> properties = new HashMap<>();
        properties.put(InjectorSourceFactory.GUICE_INJECTOR_SOURCE_KEY,
            "io.cucumber.guice.impl.LivesInChildClassLoader");
        InjectorSourceFactory injectorSourceFactory = createInjectorSourceFactory(properties);

        assertThat(injectorSourceFactory.create(), is(instanceOf(InjectorSource.class)));
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

    private static class MyChildClassLoader extends ClassLoader {

        MyChildClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.equals("io.cucumber.guice.impl.LivesInChildClassLoader")) {
                String filename = getClass().getClassLoader()
                        .getResource("io/cucumber/guice/impl/LivesInChildClassLoader.class.bin").getFile();
                File file = new File(filename);
                try {
                    FileInputStream in = new FileInputStream(file);
                    byte[] bytes = new byte[1024];
                    ByteArrayOutputStream content = new ByteArrayOutputStream();
                    while (true) {
                        int iLen = in.read(bytes);
                        content.write(bytes, 0, iLen);
                        if (iLen < 1024) {
                            break;
                        }
                    }
                    byte[] bytecode = content.toByteArray();
                    return defineClass(name, bytecode, 0, bytecode.length);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return super.loadClass(name, resolve);
        }

    }

}
