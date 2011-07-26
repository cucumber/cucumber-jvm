package cucumber.runtime.transformers;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class StandardTransformersTest {
	@Test
	public void shouldTransformBoolean() {
		Boolean transformFalse = new BooleanTransformable().transform("false",
				Locale.ENGLISH);
		Boolean transformTrue = new BooleanTransformable().transform("true",
				Locale.ENGLISH);
		Assert.assertFalse(transformFalse);
		Assert.assertTrue(transformTrue);
	}

	@Test(expected = TransformationException.class)
	public void shouldThrowTransformationExceptionWhenConvertingBoolean() {
		new BooleanTransformable().transform("vrai", Locale.ENGLISH);
	}
	
	@Test
	@Ignore("wip")
	public void shouldTransformBigDecimal() {
		BigDecimal englishBigDecimal = new BigDecimalTransformable().transform("300.15", Locale.ENGLISH);
		BigDecimal frenchBigDecimal = new BigDecimalTransformable().transform("300,15", Locale.FRENCH);
		Assert.assertEquals(new BigDecimal("300.15"), englishBigDecimal);
		Assert.assertEquals(new BigDecimal("300.15"), frenchBigDecimal);
	}

	@Test
	public void shouldTransformDate() {
		Assert.assertEquals(getDateToTest(),
				new DateTransformable().transform("11/29/2011", Locale.ENGLISH));
		Assert.assertEquals(getDateToTest(),
				new DateTransformable().transform("29/11/2011", Locale.FRENCH));
	}

	@Test(expected = TransformationException.class)
	public void shouldThrowTransformationExceptionWhenConvertingDate() {
		Assert.assertEquals(getDateToTest(),
				new DateTransformable().transform("29/11/2011", Locale.ENGLISH));
	}

	private Date getDateToTest() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2011, 10, 29, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
}
