package cucumber.runtime.java;

import cucumber.fallback.runtime.java.DefaultJavaObjectFactory;
import cucumber.io.ResourceLoader;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ObjectFactoryHolderTest {
    @After
    public void resetObjectFactory() {
        ObjectFactoryHolder.setFactory(null);
    }

    @Test
    public void uses_default_java_object_factory_if_none_is_set() throws Exception {
        ObjectFactoryHolder.setFactory(new StubObjectFactory());
        JavaBackend backend = new JavaBackend(mock(ResourceLoader.class));

        // do it by reflection to not change the API
        Field field = JavaBackend.class.getDeclaredField("objectFactory");
        field.setAccessible(true);
        assertEquals(StubObjectFactory.class, field.get(backend).getClass());

        ObjectFactoryHolder.setFactory(null);
        backend = new JavaBackend(mock(ResourceLoader.class));
        assertEquals(DefaultJavaObjectFactory.class, field.get(backend).getClass());
    }

    private static class StubObjectFactory implements ObjectFactory {
        @Override
        public void createInstances() {
        }

        @Override
        public void disposeInstances() {
        }

        @Override
        public void addClass(Class<?> clazz) {
        }

        @Override
        public <T> T getInstance(Class<T> type) {
            return null;
        }
    }
}
