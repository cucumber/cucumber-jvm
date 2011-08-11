package cucumber.runtime.transformers;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransformersTest {
    @Test
    public void shouldTransformToTheRightType() {
        Transformers transformers = new Transformers();

        assertTrue(transformers.<Boolean>transform(Locale.ENGLISH, Boolean.class, "true"));
        assertTrue(transformers.<Boolean>transform(Locale.ENGLISH, Boolean.TYPE, "true"));
        assertEquals(3000.15f, transformers.transform(Locale.ENGLISH, Float.class, "3000.15"), 0.000001);
        assertEquals(3000.15f, transformers.transform(Locale.ENGLISH, Float.TYPE, "3000.15"), 0.000001);
        assertEquals(3000.15f, transformers.transform(new Locale("NO"), Float.TYPE, "3000,15"), 0.000001);
        assertEquals(new BigDecimal("3000.15"), transformers.transform(Locale.ENGLISH, BigDecimal.class, "3000.15"));
    }
}
