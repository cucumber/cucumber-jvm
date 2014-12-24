package cucumber.runtime;

import org.junit.Test;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import static cucumber.runtime.Utils.isInstantiable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class UtilsTest {
    @Test
    public void public_non_static_inner_classes_are_not_instantiable() {
        assertFalse(isInstantiable(NonStaticInnerClass.class));
    }

    @Test
    public void public_static_inner_classes_are_instantiable() {
        assertTrue(isInstantiable(StaticInnerClass.class));
    }

    public class NonStaticInnerClass {
    }

    public static class StaticInnerClass {
    }

    @Test
    public void test_url() throws MalformedURLException {
        URL dotCucumber = Utils.toURL("foo/bar/.cucumber");
        URL url = new URL(dotCucumber, "stepdefs.json");
        assertEquals(new URL("file:foo/bar/.cucumber/stepdefs.json"), url);
    }

    @Test
    public void testInvokeDefaultClassLoader() throws Throwable {
        // Sanity check to verify that MyClass was loaded by the default class loader/
        assertEquals(ClassLoader.getSystemClassLoader(), MyClass.class.getClassLoader());
        Object target = new MyClass();

        Method publicInterfaceMethod = MyClass.class.getMethod("publicInterfaceMethod");
        assertEquals(Boolean.TRUE, Utils.invoke(target, publicInterfaceMethod, 1000));

        Method protectedAbstractMethod = MyClass.class.getDeclaredMethod("protectedAbstractMethod");
        assertEquals(Boolean.TRUE, Utils.invoke(target, protectedAbstractMethod, 1000));

        Method privateMethod = MyAbstractClass.class.getDeclaredMethod("privateMethod");
        assertEquals(Boolean.TRUE, Utils.invoke(target, privateMethod, 1000));
    }

    @Test
    public void testInvokeOtherClassLoader() throws Throwable {
        // Create a new class loader using the same URLs as the system class loader
        URL[] urls = ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs();
        ClassLoader myClassLoader = new URLClassLoader(urls, null);

        // Load the test class MyClass using our newly create class loader and create an instance.
        Class<?> myClass = myClassLoader.loadClass(MyClass.class.getName());
        Object target = myClass.getConstructor().newInstance();

        // Sanity check to verify that MyClass was loaded by the default class loader.
        assertEquals(ClassLoader.getSystemClassLoader(), MyClass.class.getClassLoader());
        // Verify that the class loader of our loaded class is different.
        assertNotEquals(MyClass.class.getClassLoader(), myClass.getClassLoader());

        Method publicInterfaceMethod = MyClass.class.getMethod("publicInterfaceMethod");
        assertEquals(Boolean.TRUE, Utils.invoke(target, publicInterfaceMethod, 1000));

        Method protectedAbstractMethod = MyClass.class.getDeclaredMethod("protectedAbstractMethod");
        assertEquals(Boolean.TRUE, Utils.invoke(target, protectedAbstractMethod, 1000));

        Method privateMethod = MyAbstractClass.class.getDeclaredMethod("privateMethod");
        assertEquals(Boolean.TRUE, Utils.invoke(target, privateMethod, 1000));
    }

    public static interface MyInterface {
        Boolean publicInterfaceMethod();
    }

    public static abstract class MyAbstractClass implements MyInterface {
        protected abstract Boolean protectedAbstractMethod();

        private Boolean privateMethod() {
            return Boolean.TRUE;
        }
    }

    public static class MyClass extends MyAbstractClass {
        @Override
        public Boolean publicInterfaceMethod() {
            return Boolean.TRUE;
        }

        @Override
        protected Boolean protectedAbstractMethod() {
            return Boolean.TRUE;
        }
    }
}
