package cucumber.runtime.transformers;

import gherkin.formatter.Argument;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransformersTest {
    @Test
    public void shouldTransformToTheRightType() {
        Transformers transformers = new Transformers();

        assertTrue(transformers.<Boolean>transform(new Argument(0, "true"), Boolean.class, Locale.ENGLISH));
        assertTrue(transformers.<Boolean>transform(new Argument(0, "true"), Boolean.TYPE, Locale.ENGLISH));
        assertEquals(3000.15f, transformers.transform(new Argument(0, "3000.15"), Float.class, Locale.ENGLISH), 0.000001);
        assertEquals(3000.15f, transformers.transform(new Argument(0, "3000.15"), Float.TYPE, Locale.ENGLISH), 0.000001);
        assertEquals(3000.15f, transformers.transform(new Argument(0, "3000,15"), Float.TYPE, new Locale("NO")), 0.000001);
        assertEquals(new BigDecimal("3000.15"), transformers.transform(new Argument(0, "3000.15"), BigDecimal.class, Locale.ENGLISH));
    }
}
