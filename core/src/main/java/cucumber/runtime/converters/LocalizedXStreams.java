package cucumber.runtime.converters;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.ConverterRegistry;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.core.DefaultConverterLookup;
import gherkin.I18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocalizedXStreams {
    private final Map<I18n, LocalizedXStream> xStreams = new HashMap<I18n, LocalizedXStream>();
    private final ClassLoader classLoader;

    public LocalizedXStreams(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public LocalizedXStream get(I18n i18n) {
        LocalizedXStream xStream = xStreams.get(i18n);
        if (xStream == null) {
            xStream = newXStream(i18n.getLocale());
            xStreams.put(i18n, xStream);
        }
        return xStream;
    }

    private LocalizedXStream newXStream(Locale locale) {
        DefaultConverterLookup lookup = new DefaultConverterLookup();
        return new LocalizedXStream(classLoader, lookup, lookup, locale);
    }

    public static class LocalizedXStream extends XStream {
        private final Locale locale;
        private static List<TimeConverter> timeConverters = new ArrayList<TimeConverter>();

        public LocalizedXStream(ClassLoader classLoader, ConverterLookup converterLookup, ConverterRegistry converterRegistry, Locale locale) {
            super(null, null, classLoader, null, converterLookup, converterRegistry);
            this.locale = locale;
            autodetectAnnotations(true);

            // Override with our own Locale-aware converters.
            register(converterRegistry, new BigDecimalConverter(locale));
            register(converterRegistry, new BigIntegerConverter(locale));
            register(converterRegistry, new ByteConverter(locale));
            register(converterRegistry, new DateConverter(locale));
            register(converterRegistry, new CalendarConverter(locale));
            register(converterRegistry, new DoubleConverter(locale));
            register(converterRegistry, new FloatConverter(locale));
            register(converterRegistry, new IntegerConverter(locale));
            register(converterRegistry, new LongConverter(locale));
        }

        private void register(ConverterRegistry lookup, SingleValueConverter converter) {
            lookup.registerConverter(new SingleValueConverterWrapperExt(converter), XStream.PRIORITY_VERY_HIGH);
        }

        public void setDateFormat(String dateFormat) {
            if (dateFormat != null) {
                List<Class> timeClasses = TimeConverter.getTimeClasses();
                for (Class timeClass : timeClasses) {
                    SingleValueConverterWrapperExt converterWrapper = (SingleValueConverterWrapperExt) getConverterLookup().lookupConverterForType(timeClass);
                    TimeConverter timeConverter = (TimeConverter) converterWrapper.getConverter();
                    timeConverter.setOnlyFormat(dateFormat, locale);
                    timeConverters.add(timeConverter);
                }
            }
        }

        public void unsetDateFormat() {
            for (TimeConverter timeConverter : timeConverters) {
                timeConverter.removeOnlyFormat();
            }
            timeConverters.clear();
        }
    }
}
