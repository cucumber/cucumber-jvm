package cuke4duke.internal;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cuke4duke.internal.java.DefaultJavaTransforms;
import cuke4duke.internal.java.JavaTransform;
import cuke4duke.internal.jvmclass.ObjectFactory;
import cuke4duke.internal.language.ArgumentsConverter;
import cuke4duke.internal.language.Transformable;

public class ArgumentsConverterTest {

    private static final Map<Class<?>, Transformable> transforms = new HashMap<Class<?>, Transformable>();
    private ArgumentsConverter converter;
    
    @BeforeClass
    public static void classSetUp() {
        ObjectFactory objectFactory = mock(ObjectFactory.class);
        for (Method method : DefaultJavaTransforms.class.getMethods()) {
            transforms.put(method.getReturnType(), new JavaTransform(method, objectFactory));
        }
    }

    @Before
    public void setUp() throws Throwable {
        converter = new ArgumentsConverter(transforms);
    }

    @Test
    public void shouldConvertFromStringToInteger() {
        Object[] convertedObject = converter.convert(new Class<?>[] { Integer.TYPE },
                new Object[] { String.format(Locale.US, "%d", Integer.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Integer.class));
    }

//    @Test
//    public void shouldConvertFromStringToLong() {
//        Object[] convertedObject = converter.convert(new Class<?>[] { Long.TYPE }, new Object[] { String.format(Locale.US, "%d", Long.MAX_VALUE) });
//        assertTrue(convertedObject[0].getClass().isAssignableFrom(Long.class));
//    }
//
//    @Test
//    public void shouldConvertFromStringToDouble() {
//        Object[] convertedObject = converter
//                .convert(new Class<?>[] { Double.TYPE }, new Object[] { String.format(Locale.US, "%f", Double.MAX_VALUE) });
//        assertTrue(convertedObject[0].getClass().isAssignableFrom(Double.class));
//    }
//
//    @Test
//    public void shouldConvertFromStringToString() {
//        Object[] convertedObject = converter.convert(new Class<?>[] {String.class}, new Object[] { "String" });
//        assertTrue(convertedObject[0].getClass().isAssignableFrom(
//                String.class));
//    }
//
//    @Test
//    public void shouldConvertFromTableToTable() {
//        Object[] convertedObject = converter.convert(new Class<?>[] {Table.class}, new Object[] { mock(Table.class) });
//        assertTrue(convertedObject[0].getClass().getInterfaces()[0].equals(Table.class));
//    }
//
//    @Test
//    public void shouldConvertFromClassToClass() {
//        Object[] convertedObject = converter.convert(new Class<?>[] {MyClass.class}, new Object[] { new MyClass() });
//        assertTrue(convertedObject[0].getClass().isAssignableFrom(
//                MyClass.class));
//    }

    private class MyClass {

    }
}
