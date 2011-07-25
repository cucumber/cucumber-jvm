package cucumber.runtime.transformers;

import gherkin.formatter.Argument;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class TransformerTest {
	@Test
	public void shouldTransformToTheRightType() {
		Argument argument = new Argument(0, "true");
		Transformer transformer = new Transformer();
		Boolean transformBool = transformer.transform(argument, Boolean.class, Locale.ENGLISH);
		Assert.assertTrue(transformBool);
		boolean transformBoolPrimitive = transformer.transform(argument, Boolean.TYPE, Locale.ENGLISH);
		Assert.assertTrue(transformBoolPrimitive);
	}
}
