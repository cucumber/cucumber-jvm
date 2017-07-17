package cucumber.runtime.xstream;

import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;
import cucumber.deps.com.thoughtworks.xstream.converters.Converter;
import cucumber.deps.com.thoughtworks.xstream.converters.ConverterLookup;
import cucumber.deps.com.thoughtworks.xstream.converters.MarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.converters.UnmarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class ExternalConverterTest {

    private List<XStreamConverter> extraConverters;
    private ClassLoader classLoader;

    @Before
    public void setUp() throws Exception {
        extraConverters = new ArrayList<XStreamConverter>();
        classLoader = Thread.currentThread().getContextClassLoader();
    }

    @Test
    public void shouldUseExtraConverter() {
        extraConverters.add(Registration.class.getAnnotation(XStreamConverter.class));
        LocalizedXStreams transformers = new LocalizedXStreams(classLoader, extraConverters);

        ConverterLookup lookup = transformers.get(Locale.US).getConverterLookup();
        Converter c = lookup.lookupConverterForType(MyClass.class);
        assertTrue(c instanceof AlwaysConverter);
    }

    @XStreamConverter(AlwaysConverter.class)
    public static class Registration {
    }

    public static class AlwaysConverter implements Converter {

        @Override
        public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext ctx) {
            throw new UnsupportedOperationException("DUMMY MARSHAL");
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext ctx) {
            throw new UnsupportedOperationException("DUMMY UNMARSHAL");
        }

        @Override
        public boolean canConvert(Class type) {
            return true;
        }

    }

    public static class MyClass {
        public final String s;

        public MyClass(String s) {
            this.s = s;
        }
    }

}
