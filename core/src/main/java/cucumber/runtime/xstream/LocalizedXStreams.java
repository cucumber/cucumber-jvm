package cucumber.runtime.xstream;

import cucumber.deps.com.thoughtworks.xstream.XStream;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;
import cucumber.deps.com.thoughtworks.xstream.converters.Converter;
import cucumber.deps.com.thoughtworks.xstream.converters.ConverterLookup;
import cucumber.deps.com.thoughtworks.xstream.converters.ConverterMatcher;
import cucumber.deps.com.thoughtworks.xstream.converters.ConverterRegistry;
import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.deps.com.thoughtworks.xstream.core.DefaultConverterLookup;
import cucumber.runtime.CucumberException;
import cucumber.runtime.ParameterInfo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocalizedXStreams {
    private final Map<Locale, LocalizedXStream> xStreamsByLocale = new HashMap<Locale, LocalizedXStream>();
    private final List<XStreamConverter> extraConverters;
    private final ClassLoader classLoader;

    public LocalizedXStreams(ClassLoader classLoader, List<XStreamConverter> extraConverters) {
        this.classLoader = classLoader;
        this.extraConverters = extraConverters;
    }

    public LocalizedXStreams(ClassLoader classLoader) {
        this(classLoader, Collections.<XStreamConverter>emptyList());
    }

    public LocalizedXStream get(Locale locale) {
        LocalizedXStream xStream = xStreamsByLocale.get(locale);
        if (xStream == null) {
            xStream = newXStream(locale);
            registerExtraConverters(xStream);
            xStreamsByLocale.put(locale, xStream);
        }
        return xStream;
    }

    private LocalizedXStream newXStream(Locale locale) {
        DefaultConverterLookup lookup = new DefaultConverterLookup();
        return new LocalizedXStream(classLoader, lookup, lookup, locale);
    }

    private void registerExtraConverters(LocalizedXStream xStream) {
        for (XStreamConverter converter : extraConverters) {
            try {
                ConverterMatcher matcher = converter.value().newInstance();
                if (matcher instanceof Converter) {
                    xStream.registerConverter((Converter) matcher, converter.priority());
                }
            } catch (InstantiationException e) {
                throw new CucumberException(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new CucumberException(e.getMessage(), e);
            }
        }
    }

    public static class LocalizedXStream extends XStream {
        private final Locale locale;
        private final ThreadLocal<List<TimeConverter>> timeConverters = new ThreadLocal<List<TimeConverter>>() {
            @Override
            protected List<TimeConverter> initialValue() {
                return new ArrayList<TimeConverter>();
            }
        };

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
            register(converterRegistry, new PatternConverter());
            converterRegistry.registerConverter(new DynamicEnumConverter(locale), XStream.PRIORITY_VERY_HIGH);

            // Must be lower priority than the ones above, but higher than xstream's built-in ReflectionConverter
            converterRegistry.registerConverter(new DynamicClassWithStringAssignableConverter(), XStream.PRIORITY_LOW);
        }

        private void register(ConverterRegistry lookup, SingleValueConverter converter) {
            lookup.registerConverter(new SingleValueConverterWrapperExt(converter), XStream.PRIORITY_VERY_HIGH);
        }

        public void setParameterInfo(ParameterInfo parameterInfo) {
            if (parameterInfo != null) {
                List<Class> timeClasses = TimeConverter.getTimeClasses();
                for (Class timeClass : timeClasses) {
                    SingleValueConverterWrapperExt converterWrapper = (SingleValueConverterWrapperExt) getConverterLookup().lookupConverterForType(timeClass);
                    TimeConverter timeConverter = (TimeConverter) converterWrapper.getConverter();
                    timeConverter.setParameterInfoAndLocale(parameterInfo, locale);
                    timeConverters.get().add(timeConverter);
                }
            }
        }

        public void unsetParameterInfo() {
            for (TimeConverter timeConverter : timeConverters.get()) {
                timeConverter.removeOnlyFormat();
            }
            timeConverters.get().clear();
        }

        public SingleValueConverter getSingleValueConverter(Type type) {
            if (Object.class.equals(type)) {
                type = String.class;
            }
            if (type instanceof Class) {
                Class clazz = (Class) type;
                ConverterLookup converterLookup = getConverterLookup();
                Converter converter = converterLookup.lookupConverterForType(clazz);
                if (converter instanceof DynamicClassBasedSingleValueConverter) {
                    return ((DynamicClassBasedSingleValueConverter) converter).converterForClass(clazz);
                }
                return converter instanceof SingleValueConverter ? (SingleValueConverter) converter : null;
            } else {
                return null;
            }
        }

        public SingleValueConverter createListConverter(String delimiter, SingleValueConverter elementConverter) {
            return new ListConverter(delimiter, elementConverter);
        }

        public Locale getLocale() {
            return locale;
        }
    }
}
