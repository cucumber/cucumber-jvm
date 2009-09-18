package cuke4duke.internal;

import static junit.framework.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cuke4duke.Table;

public class StringConverterTest {

    private StringConverter converter;

    @Before
    public void setUp() {
        converter = new StringConverter();
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
                .convert(new Class<?>[] { Double.TYPE }, new Object[] { String.format("%f", Double.MAX_VALUE) });
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
        Object[] convertedObject = converter.convert(new Class<?>[] {Table.class}, new Object[] { new MyTable() });
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
    
    private class MyTable implements Table {

        public void diffHashes(List<Map<String, String>> table) {
            // TODO Auto-generated method stub
            
        }

        public void diffHashes(List<Map<String, String>> table, Map<?, ?> options) {
            // TODO Auto-generated method stub
            
        }

        public void diffLists(List<List<String>> table) {
            // TODO Auto-generated method stub
            
        }

        public void diffLists(List<List<String>> table, Map<?, ?> options) {
            // TODO Auto-generated method stub
            
        }

        public List<Map<String, String>> hashes() {
            // TODO Auto-generated method stub
            return null;
        }

        public List<List<String>> raw() {
            // TODO Auto-generated method stub
            return null;
        }

        public List<List<String>> rows() {
            // TODO Auto-generated method stub
            return null;
        }

        public Map<String, String> rowsHash() {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
}
