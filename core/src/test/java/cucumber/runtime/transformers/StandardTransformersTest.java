package cucumber.runtime.transformers;

import java.util.Locale;

import org.junit.Assert;
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
}
