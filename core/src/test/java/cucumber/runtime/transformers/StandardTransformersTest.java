package cucumber.runtime.transformers;

import cucumber.runtime.CucumberException;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.*;

public class StandardTransformersTest {
    @Test
    public void shouldTransformBigDecimal() {
        BigDecimal englishBigDecimal = new BigDecimalTransformer(Locale.US).fromString("300.15");
        BigDecimal englishBigDecimal2 = new BigDecimalTransformer(Locale.US).fromString("30000000.15");
        BigDecimal englishInteger = new BigDecimalTransformer(Locale.US).fromString("300.15");
        BigDecimal frenchBigDecimal = new BigDecimalTransformer(Locale.FRANCE).fromString("300.0");
        assertEquals(new BigDecimal("300.15"), englishBigDecimal);
        assertEquals(new BigDecimal("30000000.15"), englishBigDecimal2);
        assertEquals(new BigDecimal("300.15"), englishInteger);
        assertEquals(new BigDecimal("300.0"), frenchBigDecimal);
    }

    @Test
    public void shouldTransformDate() {
        assertEquals(getDateToTest(), new DateTransformer(Locale.US).fromString("11/29/2011"));
        assertEquals(getDateToTest(), new DateTransformer(Locale.FRANCE).fromString("29/11/2011"));
    }

    @Test(expected = CucumberException.class)
    public void shouldThrowTransformationExceptionWhenConvertingInvalidDate() {
        assertEquals(getDateToTest(), new DateTransformer(Locale.US).fromString("29/11/2011"));
    }

    private Date getDateToTest() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2011, 10, 29, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @Test
    public void shouldTransformIntegers() {
        Integer expected = 1000;
        assertEquals(expected, new IntegerTransformer(Locale.US).fromString("1000"));
        assertEquals(expected, new IntegerTransformer(Locale.US).fromString("1,000"));
        assertEquals(expected, new IntegerTransformer(new Locale("pt")).fromString("1.000"));
    }

    @Test
    public void shouldTransformDoubles() {
        Double expected = 3000.15;
        assertEquals(expected, new DoubleTransformer(Locale.US).fromString("3000.15"));
        assertEquals(expected, new DoubleTransformer(Locale.US).fromString("3,000.15"));
        assertEquals(expected, new DoubleTransformer(new Locale("pt")).fromString("3.000,15"));
        assertEquals(expected, new DoubleTransformer(Locale.FRANCE).fromString("3000,15"));
    }

    @Test
    public void shouldTransformLongs() {
        Long expected = 8589934592L;
        assertEquals(expected, new LongTransformer(Locale.US).fromString("8589934592"));
        assertEquals(expected, new LongTransformer(Locale.US).fromString("8,589,934,592"));
    }

    @Test
    public void shouldTransformShorts() {
        Short expected = (short) 32767;
        Short expected2 = (short) -32768;
        assertEquals(expected, new ShortTransformer(Locale.US).fromString("32767"));
        assertEquals(expected, new ShortTransformer(Locale.US).fromString("32,767"));
        assertEquals(expected2, new ShortTransformer(Locale.US).fromString("-32,768"));
    }

    @Test
    public void shouldTransformBytes() {
        Byte expected = (byte) 127;
        assertEquals(expected, new ByteTransformer(Locale.US).fromString("127"));
        assertEquals(expected, new ByteTransformer(Locale.US).fromString("127"));
    }

    @Test
    public void shouldTransformFloats() {
        Float expected = 3000.15f;
        assertEquals(expected, new FloatTransformer(Locale.US).fromString("3000.15"));
        assertEquals(expected, new FloatTransformer(Locale.US).fromString("3,000.15"));
    }

    @Test
    public void shouldTransformBigInteger() {
        BigInteger expected = BigInteger.valueOf(8589934592L);
        assertEquals(expected, new BigIntegerTransformer(Locale.US).fromString("8589934592"));
        assertEquals(expected, new BigIntegerTransformer(Locale.US).fromString("8,589,934,592"));
    }

}
