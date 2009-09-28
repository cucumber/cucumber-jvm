package cuke4duke.internal;

import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

import cuke4duke.Table;

public class ArgumentsConverterTest {

    private ArgumentsConverter converter;

    @Before
    public void setUp() {
        converter = new ArgumentsConverter();
    }

    @Test
    public void shouldConvertFromStringToInteger() {
        Object[] convertedObject = converter.convert(new Class<?>[] { Integer.TYPE },
                new Object[] { String.format("%d", Integer.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Integer.class));
    }

    @Test
    public void shouldConvertFromStringToLong() {
        Object[] convertedObject = converter.convert(new Class<?>[] { Long.TYPE }, new Object[] { String.format("%d", Long.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Long.class));
    }

    @Test
    public void shouldConvertFromStringToDouble() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { Double.TYPE }, new Object[] { String.format("%e", Double.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Double.class));
    }

    @Test
    public void shouldConvertFromStringToString() {
        Object[] convertedObject = converter.convert(new Class<?>[] {String.class}, new Object[] { "String" });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(
                String.class));
    }
    
    @Test
    public void shouldConvertFromTableToTable() {
        Object[] convertedObject = converter.convert(new Class<?>[] {Table.class}, new Object[] { mock(Table.class) });
        assertTrue(convertedObject[0].getClass().getInterfaces()[0].equals(Table.class));
    }
    
    @Test
    public void shouldConvertFromClassToClass() {
        Object[] convertedObject = converter.convert(new Class<?>[] {MyClass.class}, new Object[] { new MyClass() });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(
                MyClass.class));
    }
    
    private class MyClass {
        
    }
}
