package cucumber.runtime.transformers;

import gherkin.formatter.Argument;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class TransformerTest {
	@Test
	public void shouldTransformToTheRightType() {
		Argument argument = new Argument(0, "true");
		Transformer transformer = new Transformer();
		Locale english = Locale.ENGLISH;
		Boolean transformBool = transformer.transform(argument, Boolean.class, english);
		Assert.assertTrue(transformBool);
		boolean transformBoolPrimitive = transformer.transform(argument, Boolean.TYPE, english);
		Assert.assertTrue("Boolean primitive transformation", transformBoolPrimitive);
		Assert.assertEquals("Float class transformation", Float.valueOf(3000.15f), transformer.transform(new Argument(0, "3000.15"), Float.class, english));
		Assert.assertEquals("Float primitive transformation", Float.valueOf(3000.15f), transformer.transform(new Argument(0, "3000.15"), Float.TYPE, english));
		Assert.assertEquals("BigDecimal transformation", new BigDecimal("3000.15"), transformer.transform(new Argument(0, "3000.15"), BigDecimal.class, english));
	}
}
