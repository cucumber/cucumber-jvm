package cucumber.runtime.transformers;

import cuke4duke.internal.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StandardTransformersTest {
    @Test
    public void shouldTransformBoolean() {
        Boolean transformFalse = new BooleanTransformer().transform(Locale.ENGLISH, "false");
        Boolean transformTrue = new BooleanTransformer().transform(Locale.ENGLISH, "true");
        assertFalse(transformFalse);
        assertTrue(transformTrue);
    }

    @Test(expected = TransformationException.class)
    public void shouldThrowTransformationExceptionWhenConvertingBoolean() {
        new BooleanTransformer().transform(Locale.ENGLISH, "vrai");
    }

    @Test
    public void shouldTransformBigDecimal() {
        BigDecimal englishBigDecimal = new BigDecimalTransformer().transform(Locale.ENGLISH, "300.15");
        BigDecimal englishBigDecimal2 = new BigDecimalTransformer().transform(Locale.ENGLISH, "30000000.15");
        BigDecimal englishInteger = new BigDecimalTransformer().transform(Locale.ENGLISH, "300.15");
        BigDecimal frenchBigDecimal = new BigDecimalTransformer().transform(Locale.FRENCH, "300.0");
        assertEquals(new BigDecimal("300.15"), englishBigDecimal);
        assertEquals(new BigDecimal("30000000.15"), englishBigDecimal2);
        assertEquals(new BigDecimal("300.15"), englishInteger);
        assertEquals(new BigDecimal("300.0"), frenchBigDecimal);
    }

    @Test
    public void shouldTransformDate() {
        assertEquals(getDateToTest(), new DateTransformer().transform(Locale.ENGLISH, "11/29/2011"));
        assertEquals(getDateToTest(), new DateTransformer().transform(Locale.FRENCH, "29/11/2011"));
    }

    @Test(expected = TransformationException.class)
    public void shouldThrowTransformationExceptionWhenConvertingInvalidDate() {
        assertEquals(getDateToTest(), new DateTransformer().transform(Locale.ENGLISH, "29/11/2011"));
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
        assertEquals(expected, new IntegerTransformer().transform(Locale.ENGLISH, "1000"));
        assertEquals(expected, new IntegerTransformer().transform(Locale.ENGLISH, "1,000"));
        assertEquals(expected, new IntegerTransformer().transform(Utils.localeFor("pt"), "1.000"));
    }

    @Test
    public void shouldTransformDoubles() {
        Double expected = 3000.15;
        assertEquals(expected, new DoubleTransformer().transform(Locale.ENGLISH, "3000.15"));
        assertEquals(expected, new DoubleTransformer().transform(Locale.ENGLISH, "3,000.15"));
        assertEquals(expected, new DoubleTransformer().transform(Utils.localeFor("pt"), "3.000,15"));
        assertEquals(expected, new DoubleTransformer().transform(Locale.FRENCH, "3000,15"));
    }

    @Test
    public void shouldTransformLongs() {
        Long expected = 8589934592L;
        assertEquals(expected, new LongTransformer().transform(Locale.ENGLISH, "8589934592"));
        assertEquals(expected, new LongTransformer().transform(Locale.ENGLISH, "8,589,934,592"));
    }

    @Test
    public void shouldTransformShorts() {
        Short expected = (short) 32767;
        Short expected2 = (short) -32768;
        assertEquals(expected, new ShortTransformer().transform(Locale.ENGLISH, "32767"));
        assertEquals(expected, new ShortTransformer().transform(Locale.ENGLISH, "32,767"));
        assertEquals(expected2, new ShortTransformer().transform(Locale.ENGLISH, "-32,768"));
    }

    @Test
    public void shouldTransformBytes() {
        Byte expected = (byte) 127;
        assertEquals(expected, new ByteTransformer().transform(Locale.ENGLISH, "127"));
        assertEquals(expected, new ByteTransformer().transform(Locale.ENGLISH, "127"));
    }

    @Test
    public void shouldTransformChars() {
        Character expected = 'C';
        assertEquals(expected, new CharacterTransformer().transform(Locale.ENGLISH, "Cedric"));
        assertEquals(expected, new CharacterTransformer().transform(Locale.ENGLISH, "C"));
    }

    @Test
    public void shouldTransformFloats() {
        Float expected = 3000.15f;
        assertEquals(expected, new FloatTransformer().transform(Locale.ENGLISH, "3000.15"));
        assertEquals(expected, new FloatTransformer().transform(Locale.ENGLISH, "3,000.15"));
    }

    @Test
    public void shouldTransformBigInteger() {
        BigInteger expected = BigInteger.valueOf(8589934592L);
        assertEquals(expected, new BigIntegerTransformer().transform(Locale.ENGLISH, "8589934592"));
        assertEquals(expected, new BigIntegerTransformer().transform(Locale.ENGLISH, "8,589,934,592"));
    }

}
