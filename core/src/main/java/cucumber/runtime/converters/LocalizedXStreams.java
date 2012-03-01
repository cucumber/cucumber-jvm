package cucumber.runtime.converters;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.core.DefaultConverterLookup;
import gherkin.I18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocalizedXStreams {
    private final Map<I18n, XStream> xStreams = new HashMap<I18n, XStream>();
    private final ClassLoader classLoader;

    public LocalizedXStreams(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public XStream get(I18n i18n) {
        XStream xStream = xStreams.get(i18n);
        if (xStream == null) {
            xStream = newXStream(i18n.getLocale());
            xStreams.put(i18n, xStream);
        }
        return xStream;
    }

    private XStream newXStream(Locale locale) {
        DefaultConverterLookup lookup = new DefaultConverterLookup();

        // XStream's registers all the default converters.
        XStream xStream = new XStream(null, null, classLoader, null, lookup, lookup);
        xStream.autodetectAnnotations(true);

        // Override with our own Locale-aware converters.
        register(lookup, new BigDecimalConverter(locale));
        register(lookup, new BigIntegerConverter(locale));
        register(lookup, new ByteConverter(locale));
        register(lookup, new DateConverter(locale));
        register(lookup, new CalendarConverter(locale));
        register(lookup, new DoubleConverter(locale));
        register(lookup, new FloatConverter(locale));
        register(lookup, new IntegerConverter(locale));
        register(lookup, new LongConverter(locale));
        return xStream;
    }

    private void register(DefaultConverterLookup lookup, SingleValueConverter converter) {
        lookup.registerConverter(new SingleValueConverterWrapperExt(converter), XStream.PRIORITY_VERY_HIGH);
    }
}
