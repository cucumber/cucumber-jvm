package cucumber.runtime.converters;

import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConvertersTest {
    @Test
    public void shouldTransformToTheRightType() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        LocalizedXStreams transformers = new LocalizedXStreams(classLoader);

        ConverterLookup en = transformers.get(Locale.ENGLISH).getConverterLookup();
        assertTrue((Boolean) ((SingleValueConverter) en.lookupConverterForType(Boolean.class)).fromString("true"));
        assertTrue((Boolean) ((SingleValueConverter) en.lookupConverterForType(Boolean.TYPE)).fromString("true"));
        assertEquals(3000.15f, (Float) ((SingleValueConverter) en.lookupConverterForType(Float.class)).fromString("3000.15"), 0.000001);
        assertEquals(3000.15f, (Float) ((SingleValueConverter) en.lookupConverterForType(Float.TYPE)).fromString("3000.15"), 0.000001);
        assertEquals(new BigDecimal("3000.15"), ((SingleValueConverter) en.lookupConverterForType(BigDecimal.class)).fromString("3000.15"));

        ConverterLookup no = transformers.get(new Locale("NO")).getConverterLookup();
        assertEquals(3000.15f, (Float) ((SingleValueConverter) no.lookupConverterForType(Float.TYPE)).fromString("3000,15"), 0.000001);
    }
}
