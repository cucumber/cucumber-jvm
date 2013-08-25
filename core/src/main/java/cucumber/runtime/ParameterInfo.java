package cucumber.runtime;

import cucumber.api.Delimiter;
import cucumber.api.Format;
import cucumber.api.Transform;
import cucumber.api.Transformer;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;
import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.runtime.xstream.LocalizedXStreams;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class composes all interesting parameter information into one object.
 */
public class ParameterInfo {
    public static final String DEFAULT_DELIMITER = ",\\s?";

    private final Type type;
    private final String format;
    private final String delimiter;
    private final Transformer transformer;

    public static List<ParameterInfo> fromMethod(Method method) {
        List<ParameterInfo> result = new ArrayList<ParameterInfo>();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            String format = null;
            String delimiter = DEFAULT_DELIMITER;
            Transformer transformer = null;
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof Format) {
                    format = ((Format) annotation).value();
                }
                if (annotation instanceof Delimiter) {
                    delimiter = ((Delimiter) annotation).value();
                }
                if (annotation instanceof Transform) {
                    try {
                        transformer = ((Transform) annotation).value().newInstance();
                    } catch (InstantiationException e) {
                        throw new CucumberException(e);
                    } catch (IllegalAccessException e) {
                        throw new CucumberException(e);
                    }
                }
            }
            result.add(new ParameterInfo(genericParameterTypes[i], format, delimiter, transformer));
        }
        return result;
    }

    public ParameterInfo(Type type, String format, String delimiter, Transformer transformer) {
        this.type = type;
        this.format = format;
        this.delimiter = delimiter;
        this.transformer = transformer;
    }

    public Class<?> getRawType() {
        return getRawType(type);
    }

    private Class<?> getRawType(Type type) {
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            return (Class<?>) type;
        }
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString();
    }

    public Object convert(String value, LocalizedXStreams.LocalizedXStream xStream) {
        try {
            xStream.setParameterType(this);
            SingleValueConverter converter;
            xStream.processAnnotations(getRawType());
            xStream.autodetectAnnotations(true); // Needed to unlock annotation processing

            if (transformer != null) {
                transformer.setParameterInfoAndLocale(this, xStream.getLocale());
                converter = transformer;
            } else {
                if (List.class.isAssignableFrom(getRawType())) {
                    converter = getListConverter(type, xStream);
                } else {
                    converter = getConverter(getRawType(), xStream);
                }
                if (converter == null) {
                    throw new CucumberException(String.format(
                            "Don't know how to convert \"%s\" into %s.\n" +
                                    "Try writing your own converter:\n" +
                                    "\n" +
                                    "@%s(%sConverter.class)\n" +
                                    "public class %s {}\n",
                            value,
                            getRawType().getName(),
                            XStreamConverter.class.getName(),
                            getRawType().getSimpleName(),
                            getRawType().getSimpleName()
                    ));
                }
            }
            return converter.fromString(value);
        } finally {
            xStream.unsetParameterInfo();
        }
    }

    private SingleValueConverter getListConverter(Type type, LocalizedXStreams.LocalizedXStream xStream) {
        Class elementType = type instanceof ParameterizedType
                ? getRawType(((ParameterizedType) type).getActualTypeArguments()[0])
                : Object.class;

        SingleValueConverter elementConverter = getConverter(elementType, xStream);
        if (elementConverter == null) {
            return null;
        } else {
            return xStream.createListConverter(delimiter, elementConverter);
        }
    }

    private SingleValueConverter getConverter(Class<?> type, LocalizedXStreams.LocalizedXStream xStream) {
        if (type.isEnum()) {
            return xStream.createEnumConverter((Class<? extends Enum>) type);
        } else {
            return xStream.getSingleValueConverter(type);
        }
    }

    public String getFormat() {
        return format;
    }
}
