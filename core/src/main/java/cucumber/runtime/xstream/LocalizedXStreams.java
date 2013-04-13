package cucumber.runtime.xstream;

import cucumber.deps.com.thoughtworks.xstream.XStream;
import cucumber.deps.com.thoughtworks.xstream.converters.Converter;
import cucumber.deps.com.thoughtworks.xstream.converters.ConverterLookup;
import cucumber.deps.com.thoughtworks.xstream.converters.ConverterRegistry;
import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.deps.com.thoughtworks.xstream.core.DefaultConverterLookup;
import cucumber.runtime.ParameterInfo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocalizedXStreams {
    private final Map<Locale, LocalizedXStream> xStreamsByLocale = new HashMap<Locale, LocalizedXStream>();
    private final ClassLoader classLoader;

    public LocalizedXStreams(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public LocalizedXStream get(Locale locale) {
        LocalizedXStream xStream = xStreamsByLocale.get(locale);
        if (xStream == null) {
            xStream = newXStream(locale);
            xStreamsByLocale.put(locale, xStream);
        }
        return xStream;
    }

    private LocalizedXStream newXStream(Locale locale) {
        DefaultConverterLookup lookup = new DefaultConverterLookup();
        return new LocalizedXStream(classLoader, lookup, lookup, locale);
    }

    public static class LocalizedXStream extends XStream {
        private final Locale locale;
        private static final List<TimeConverter> timeConverters = new ArrayList<TimeConverter>();

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

            // Must be lower priority than the ones above, but higher than xstream's built-in ReflectionConverter
            converterRegistry.registerConverter(new SingleValueConverterWrapperExt(new ClassWithStringAssignableConstructorConverter()), XStream.PRIORITY_LOW);
        }

        private void register(ConverterRegistry lookup, SingleValueConverter converter) {
            lookup.registerConverter(new SingleValueConverterWrapperExt(converter), XStream.PRIORITY_VERY_HIGH);
        }

        public void setParameterType(ParameterInfo parameterInfo) {
            if (parameterInfo != null) {
                List<Class> timeClasses = TimeConverter.getTimeClasses();
                for (Class timeClass : timeClasses) {
                    SingleValueConverterWrapperExt converterWrapper = (SingleValueConverterWrapperExt) getConverterLookup().lookupConverterForType(timeClass);
                    TimeConverter timeConverter = (TimeConverter) converterWrapper.getConverter();
                    timeConverter.setParameterInfoAndLocale(parameterInfo, locale);
                    timeConverters.add(timeConverter);
                }
            }
        }

        public void unsetParameterInfo() {
            for (TimeConverter timeConverter : timeConverters) {
                timeConverter.removeOnlyFormat();
            }
            timeConverters.clear();
        }

        public SingleValueConverter getSingleValueConverter(Type type) {
            if (Object.class.equals(type)) {
                type = String.class;
            }
            if (type instanceof Class) {
                Class clazz = (Class) type;
                if (clazz.isEnum()) {
                    return new EnumConverter(locale, clazz);
                }
                ConverterLookup converterLookup = getConverterLookup();
                Converter converter = converterLookup.lookupConverterForType((Class) type);
                return converter instanceof SingleValueConverter ? (SingleValueConverter) converter : null;
            } else {
                return null;
            }
        }

        public SingleValueConverter createListConverter(String delimiter, SingleValueConverter elementConverter) {
            return new ListConverter(delimiter, elementConverter);
        }

        public SingleValueConverter createEnumConverter(Class<? extends Enum> type) {
            return new EnumConverter(locale, type);
        }

        public Locale getLocale() {
            return locale;
        }
    }
}
