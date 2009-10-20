package cuke4duke.internal.jvmclass;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cuke4duke.StepMother;
import cuke4duke.Table;
import cuke4duke.internal.java.JavaAnalyzer;
import cuke4duke.internal.language.Transformable;

public class ArgumentsConverterTest {

    private Map<Class<?>, Transformable> transforms;
    private ArgumentsConverter converter;

    @BeforeClass
    public static void classSetUp() throws Throwable {

    }

    @Before
    public void setUp() throws Throwable {
        System.setProperty("cuke4duke.objectFactory", "cuke4duke.internal.jvmclass.PicoFactory");
        ClassLanguage classLanguage = new ClassLanguage(mock(ClassLanguageMixin.class), mock(StepMother.class), Arrays.asList(new ClassAnalyzer[] { new JavaAnalyzer() }));
        classLanguage.prepareScenario();
        transforms = classLanguage.getTransforms();
        converter = new ArgumentsConverter(transforms);
    }

    @Test
    public void shouldConvertFromStringToInt() {
        Object[] convertedObject = converter.convert(new Class<?>[] { Integer.TYPE },
                new Object[] { String.format(Locale.US, "%d", Integer.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Integer.class));
    }

    @Test
    public void shouldConvertFromStringToInteger() {
        Object[] convertedObject = converter.convert(new Class<?>[] { Integer.class },
                new Object[] { String.format(Locale.US, "%d", Integer.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Integer.class));
    }

    @Test
    public void shouldConvertFromStringToLongPrimitive() {
        Object[] convertedObject = converter.convert(new Class<?>[] { Long.TYPE }, new Object[] { String.format(Locale.US, "%d", Long.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Long.class));
    }

    @Test
    public void shouldConvertFromStringToLong() {
        Object[] convertedObject = converter.convert(new Class<?>[] { Long.class }, new Object[] { String.format(Locale.US, "%d", Long.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Long.class));
    }

    @Test
    public void shouldConvertFromStringToDoublePrimitive() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { Double.TYPE }, new Object[] { String.format(Locale.US, "%f", Double.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Double.class));
    }

    @Test
    public void shouldConvertFromStringToDouble() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { Double.class }, new Object[] { String.format(Locale.US, "%f", Double.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Double.class));
    }

    @Test
    public void shouldConvertFromStringToFloatPrimitive() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { Float.TYPE }, new Object[] { String.format(Locale.US, "%f", Float.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Float.class));
    }

    @Test
    public void shouldConvertFromStringToFloat() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { Float.class }, new Object[] { String.format(Locale.US, "%f", Float.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Float.class));
    }

    @Test
    public void shouldConvertFromStringToShortPrimitive() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { Short.TYPE }, new Object[] { String.format(Locale.US, "%d", Short.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Short.class));
    }

    @Test
    public void shouldConvertFromStringToShort() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { Short.class }, new Object[] { String.format(Locale.US, "%d", Short.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Short.class));
    }

    @Test
    public void shouldConvertFromStringToBytePrimitive() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { Byte.TYPE }, new Object[] { String.format(Locale.US, "%d", Byte.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Byte.class));
    }

    @Test
    public void shouldConvertFromStringToByte() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { Byte.class }, new Object[] { String.format(Locale.US, "%d", Byte.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Byte.class));
    }

    @Test
    public void shouldConvertFromStringToChar() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { Character.TYPE }, new Object[] { String.format(Locale.US, "%c", Character.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Character.class));
    }

    @Test
    public void shouldConvertFromStringToCharacter() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { Character.class }, new Object[] { String.format(Locale.US, "%c", Character.MAX_VALUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Character.class));
    }

    @Test
    public void shouldConvertFromStringToBigDecimal() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { BigDecimal.class }, new Object[] { String.format(Locale.US, "%f", BigDecimal.TEN) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(BigDecimal.class));
    }

    @Test
    public void shouldConvertFromStringToBigInteger() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { BigInteger.class }, new Object[] { String.format(Locale.US, "%d", BigInteger.TEN) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(BigInteger.class));
    }

    @Test
    public void shouldConvertFromStringToBooleanPrimitive() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { Boolean.TYPE }, new Object[] { String.format(Locale.US, "%b", Boolean.TRUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Boolean.class));
        assertTrue((Boolean)convertedObject[0]);
    }

    @Test
    public void shouldConvertFromStringToBoolean() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { Boolean.class }, new Object[] { String.format(Locale.US, "%b", Boolean.TRUE) });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Boolean.class));
    }

    @Test
    public void shouldConvertFromStringToString() {
        Object[] convertedObject = converter.convert(new Class<?>[] { String.class }, new Object[] { "String" });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(
                String.class));
    }

    @Test
    public void shouldConvertFromTableToTable() {
        Object[] convertedObject = converter.convert(new Class<?>[] { Table.class }, new Object[] { mock(Table.class) });
        assertTrue(convertedObject[0].getClass().getInterfaces()[0].equals(Table.class));
    }

    @Test
    public void shouldConvertFromClassToClass() {
        Object[] convertedObject = converter.convert(new Class<?>[] { MyClass.class }, new Object[] { new MyClass() });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(
                MyClass.class));
    }

    private class MyClass {
    }

}
