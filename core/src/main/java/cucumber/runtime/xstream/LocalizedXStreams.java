package cucumber.runtime.xstream;

import cucumber.api.Transform;
import cucumber.api.Transformer;
import cucumber.deps.com.thoughtworks.xstream.XStream;
import cucumber.deps.com.thoughtworks.xstream.converters.Converter;
import cucumber.deps.com.thoughtworks.xstream.converters.ConverterLookup;
import cucumber.deps.com.thoughtworks.xstream.converters.ConverterRegistry;
import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.deps.com.thoughtworks.xstream.converters.MarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.converters.UnmarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.core.DefaultConverterLookup;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import cucumber.deps.com.thoughtworks.xstream.mapper.Mapper;
import cucumber.deps.com.thoughtworks.xstream.mapper.MapperWrapper;
import cucumber.runtime.CucumberException;
import cucumber.runtime.ParameterInfo;

import java.lang.reflect.Field;
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

    public static class TransformerBasedConverter implements Converter {
        private Transformer t;

        public TransformerBasedConverter(Transformer t) {
            this.t = t;
        }

        @Override
        public void marshal(Object o, HierarchicalStreamWriter hierarchicalStreamWriter, MarshallingContext marshallingContext) {
            hierarchicalStreamWriter.setValue(t.toString(o));
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader hierarchicalStreamReader, UnmarshallingContext unmarshallingContext) {
            return t.fromString(hierarchicalStreamReader.getValue());
        }

        @Override
        public boolean canConvert(Class aClass) {
            return t.canConvert(aClass);
        }
    }

    public static class TransformAwareMapper extends MapperWrapper {
        private Mapper wrapped;

        public TransformAwareMapper(Mapper wrapped) {
            super(wrapped);
            this.wrapped = wrapped;
        }

        public Converter getLocalConverter(Class definedIn, String fieldName) {

            try {
                final Field field = definedIn.getDeclaredField(fieldName);
                field.setAccessible(true);
                final Transform transform = field.getAnnotation(Transform.class);
                if (transform != null) {
                    final Class<? extends Transformer<?>> transformClass = transform.value();

                    final Transformer<?> transformer = transformClass.newInstance();

                    return new TransformerBasedConverter(transformer);
                }
            } catch (NoSuchFieldException e) {
                throw new CucumberException(e);
            } catch (InstantiationException e) {
                throw new CucumberException(e);
            } catch (IllegalAccessException e) {
                throw new CucumberException(e);
            }

            return wrapped.getLocalConverter(definedIn, fieldName);
        }

        @Override
        public String serializedClass(Class type) {
            return super.serializedClass(type);
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

        protected MapperWrapper wrapMapper(MapperWrapper next) {
            MapperWrapper superWrapper = super.wrapMapper(next);

            MapperWrapper wrapper = new TransformAwareMapper(superWrapper);

            return wrapper;
        }

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
