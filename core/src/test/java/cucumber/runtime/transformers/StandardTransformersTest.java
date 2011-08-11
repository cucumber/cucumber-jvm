package cucumber.runtime.transformers;

import cuke4duke.internal.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StandardTransformersTest {
    @Test
    public void shouldTransformBoolean() {
        Boolean transformFalse = new BooleanTransformer().transform("false", Locale.ENGLISH);
        Boolean transformTrue = new BooleanTransformer().transform("true", Locale.ENGLISH);
        Assert.assertFalse(transformFalse);
        Assert.assertTrue(transformTrue);
    }

    @Test(expected = TransformationException.class)
    public void shouldThrowTransformationExceptionWhenConvertingBoolean() {
        new BooleanTransformer().transform("vrai", Locale.ENGLISH);
    }

    @Test
    public void shouldTransformBigDecimal() {
        BigDecimal englishBigDecimal = new BigDecimalTransformer().transform("300.15",
                Locale.ENGLISH);
        BigDecimal englishBigDecimal2 = new BigDecimalTransformer().transform("30000000.15",
                Locale.ENGLISH);
        BigDecimal englishInteger = new BigDecimalTransformer().transform("300", Locale.ENGLISH);
        BigDecimal frenchBigDecimal = new BigDecimalTransformer().transform("300,15",
                Locale.FRENCH);
        Assert.assertEquals(new BigDecimal("300.15"), englishBigDecimal);
        Assert.assertEquals(new BigDecimal("30000000.15"), englishBigDecimal2);
        Assert.assertEquals(new BigDecimal("300.15"), frenchBigDecimal);
        Assert.assertEquals(new BigDecimal("300.0"), englishInteger);
    }

    @Test
    public void shouldTransformDate() {
        Assert.assertEquals(getDateToTest(),
                new DateTransformer().transform("11/29/2011", Locale.ENGLISH));
        Assert.assertEquals(getDateToTest(),
                new DateTransformer().transform("29/11/2011", Locale.FRENCH));
    }

    @Test(expected = TransformationException.class)
    public void shouldThrowTransformationExceptionWhenConvertingInvalidDate() {
        Assert.assertEquals(getDateToTest(),
                new DateTransformer().transform("29/11/2011", Locale.ENGLISH));
    }

    private Date getDateToTest() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2011, 10, 29, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @Test
    public void shouldTransformIntegers() {
        Integer expected = Integer.valueOf(1000);
        Assert.assertEquals(expected, new IntegerTransformer().transform("1000", Locale.ENGLISH));
        Assert.assertEquals(expected, new IntegerTransformer().transform("1,000", Locale.ENGLISH));
        Assert.assertEquals(expected,
                new IntegerTransformer().transform("1.000", Utils.localeFor("pt")));
    }

    @Test
    public void shouldTransformDoubles() {
        Double expected = Double.valueOf(3000.15);
        Assert.assertEquals(expected,
                new DoubleTransformer().transform("3000.15", Locale.ENGLISH));
        Assert.assertEquals(expected,
                new DoubleTransformer().transform("3,000.15", Locale.ENGLISH));
        Assert.assertEquals(expected,
                new DoubleTransformer().transform("3.000,15", Utils.localeFor("pt")));
        Assert.assertEquals(expected,
                new DoubleTransformer().transform("3000,15", Locale.FRENCH));
    }

    @Test
    public void shouldTransformLongs() {
        Long expected = Long.valueOf(8589934592L);
        Assert.assertEquals(expected, new LongTransformer().transform("8589934592", Locale.ENGLISH));
        Assert.assertEquals(expected, new LongTransformer().transform("8,589,934,592", Locale.ENGLISH));
    }

    @Test
    public void shouldTransformShorts() {
        short exp = 32767;
        short exp2 = -32768;
        Short expected = Short.valueOf(exp);
        Short expected2 = Short.valueOf(exp2);
        Assert.assertEquals(expected, new ShortTransformer().transform("32767", Locale.ENGLISH));
        Assert.assertEquals(expected, new ShortTransformer().transform("32,767", Locale.ENGLISH));
        Assert.assertEquals(expected2, new ShortTransformer().transform("-32,768", Locale.ENGLISH));
    }

    @Test
    public void shouldTransformBytes() {
        byte exp = 127;
        Byte expected = Byte.valueOf(exp);
        Assert.assertEquals(expected, new ByteTransformer().transform("127", Locale.ENGLISH));
        Assert.assertEquals(expected, new ByteTransformer().transform("127", Locale.ENGLISH));
    }

    @Test
    public void shouldTransformChars() {
        Character expected = Character.valueOf('C');
        Assert.assertEquals(expected, new CharacterTransformer().transform("Cedric", Locale.ENGLISH));
        Assert.assertEquals(expected, new CharacterTransformer().transform("C", Locale.ENGLISH));
    }

    @Test
    public void shouldTransformFloats() {
        Float expected = Float.valueOf(3000.15f);
        Assert.assertEquals(expected, new FloatTransformer().transform("3000.15", Locale.ENGLISH));
        Assert.assertEquals(expected, new FloatTransformer().transform("3,000.15", Locale.ENGLISH));
    }

    @Test
    public void shouldTransformBigInteger() {
        BigInteger expected = BigInteger.valueOf(8589934592L);
        Assert.assertEquals(expected, new BigIntegerTransformer().transform("8589934592", Locale.ENGLISH));
        Assert.assertEquals(expected, new BigIntegerTransformer().transform("8,589,934,592", Locale.ENGLISH));
    }

}
