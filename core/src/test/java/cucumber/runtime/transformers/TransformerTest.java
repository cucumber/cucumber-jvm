package cucumber.runtime.transformers;

import gherkin.formatter.Argument;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransformerTest {
    @Test
    public void shouldTransformToTheRightType() {
        Transformer transformer = new Transformer();

        assertTrue(transformer.<Boolean>transform(new Argument(0, "true"), Boolean.class, Locale.ENGLISH));
        assertTrue(transformer.<Boolean>transform(new Argument(0, "true"), Boolean.TYPE, Locale.ENGLISH));
        assertEquals(3000.15f, transformer.transform(new Argument(0, "3000.15"), Float.class, Locale.ENGLISH));
        assertEquals(3000.15f, transformer.transform(new Argument(0, "3000.15"), Float.TYPE, Locale.ENGLISH));
        assertEquals(3000.15f, transformer.transform(new Argument(0, "3000,15"), Float.TYPE, new Locale("NO")));
        assertEquals(new BigDecimal("3000.15"), transformer.transform(new Argument(0, "3000.15"), BigDecimal.class, Locale.ENGLISH));
    }
}
