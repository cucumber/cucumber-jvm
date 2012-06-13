package cucumber.runtime.converters;

import com.thoughtworks.xstream.converters.ConversionException;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class StandardConvertersTest {

    @Test
    public void shouldThrowInformativeErrorMessageWhenTransformationFails() {
        try {
            new IntegerConverter(new Locale("pt")).fromString("hello");
            fail();
        } catch (ConversionException e) {
            assertEquals("Couldn't convert \"hello\" to an instance of: [class java.lang.Integer, int]", e.getShortMessage());
        }
    }

    @Test
    public void shouldConvertToNullWhenArgumentIsNull() {
        assertEquals(null, new IntegerConverter(Locale.US).fromString(null));
    }

    @Test
    public void shouldTransformBigDecimal() {
        BigDecimal englishBigDecimal = new BigDecimalConverter(Locale.US).fromString("300.15");
        BigDecimal englishBigDecimal2 = new BigDecimalConverter(Locale.US).fromString("30000000.15");
        BigDecimal englishInteger = new BigDecimalConverter(Locale.US).fromString("300.15");
        BigDecimal frenchBigDecimal = new BigDecimalConverter(Locale.FRANCE).fromString("300.0");
        assertEquals(new BigDecimal("300.15"), englishBigDecimal);
        assertEquals(new BigDecimal("30000000.15"), englishBigDecimal2);
        assertEquals(new BigDecimal("300.15"), englishInteger);
        assertEquals(new BigDecimal("300.0"), frenchBigDecimal);
    }

    @Test
    public void shouldTransformDate() {
        assertEquals(getDateToTest(), new DateConverter(Locale.US).fromString("11/29/2011"));
        assertEquals(getDateToTest(), new DateConverter(Locale.FRANCE).fromString("29/11/2011"));
    }

    @Test(expected = ConversionException.class)
    public void shouldThrowConversionExceptionWhenConvertingInvalidDate() {
        assertEquals(getDateToTest(), new DateConverter(Locale.US).fromString("29/11/2011"));
    }

    private Date getDateToTest() {
        Calendar calendar = Calendar.getInstance(Locale.US);
        calendar.set(2011, 10, 29, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @Test
    public void shouldTransformIntegers() {
        Integer expected = 1000;
        assertEquals(expected, new IntegerConverter(Locale.US).fromString("1000"));
        assertEquals(expected, new IntegerConverter(Locale.US).fromString("1,000"));
        assertEquals(expected, new IntegerConverter(new Locale("pt")).fromString("1.000"));
    }

    @Test
    public void shouldTransformDoubles() {
        Double expected = 3000.15;
        assertEquals(expected, new DoubleConverter(Locale.US).fromString("3000.15"));
        assertEquals(expected, new DoubleConverter(Locale.US).fromString("3,000.15"));
        assertEquals(expected, new DoubleConverter(new Locale("pt")).fromString("3.000,15"));
        assertEquals(expected, new DoubleConverter(Locale.FRANCE).fromString("3000,15"));
        assertEquals(null, new DoubleConverter(Locale.FRANCE).fromString(""));
    }

    @Test
    public void shouldTransformLongs() {
        Long expected = 8589934592L;
        assertEquals(expected, new LongConverter(Locale.US).fromString("8589934592"));
        assertEquals(expected, new LongConverter(Locale.US).fromString("8,589,934,592"));
    }

    @Test
    public void shouldTransformShorts() {
        Short expected = (short) 32767;
        Short expected2 = (short) -32768;
        assertEquals(expected, new ShortConverter(Locale.US).fromString("32767"));
        assertEquals(expected, new ShortConverter(Locale.US).fromString("32,767"));
        assertEquals(expected2, new ShortConverter(Locale.US).fromString("-32,768"));
    }

    @Test
    public void shouldTransformBytes() {
        Byte expected = (byte) 127;
        assertEquals(expected, new ByteConverter(Locale.US).fromString("127"));
        assertEquals(expected, new ByteConverter(Locale.US).fromString("127"));
    }

    @Test
    public void shouldTransformFloats() {
        Float expected = 3000.15f;
        assertEquals(expected, new FloatConverter(Locale.US).fromString("3000.15"));
        assertEquals(expected, new FloatConverter(Locale.US).fromString("3,000.15"));
    }

    @Test
    public void shouldTransformBigInteger() {
        BigInteger expected = BigInteger.valueOf(8589934592L);
        assertEquals(expected, new BigIntegerConverter(Locale.US).fromString("8589934592"));
        assertEquals(expected, new BigIntegerConverter(Locale.US).fromString("8,589,934,592"));
    }

    @Test
    public void shouldTransformEnums() {
        EnumConverter enumConverter = new EnumConverter(Locale.US, Color.class);
        assertEquals(Color.GREEN, enumConverter.fromString("GREEN"));
        assertEquals(Color.GREEN, enumConverter.fromString("Green"));
        assertEquals(Color.GREEN, enumConverter.fromString("GrEeN"));
        assertEquals(Color.RED, enumConverter.fromString("red"));
    }

    @Test
    public void shouldListAllowedEnumsWhenConversionFails() {
        EnumConverter enumConverter = new EnumConverter(Locale.US, Color.class);
        try {
            enumConverter.fromString("YELLOW");
            fail();
        } catch (ConversionException expected) {
            String expectedMessage = "Couldn't convert YELLOW to cucumber.runtime.converters.StandardConvertersTest$Color. Legal values are [RED, GREEN, BLUE]";
            if(!expected.getMessage().startsWith(expectedMessage)) {
                fail("'" + expected.getMessage() + "' didn't start with '" + expectedMessage + "'");
            }
        }
    }

    public static enum Color {
        RED, GREEN, BLUE
    }
}
