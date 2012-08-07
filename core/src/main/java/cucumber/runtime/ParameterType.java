package cucumber.runtime;

import cucumber.DateFormat;
import cucumber.Delimiter;
import cucumber.api.Transform;
import cucumber.runtime.converters.EnumConverter;
import cucumber.runtime.converters.ListConverter;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.runtime.xstream.annotations.XStreamConverter;
import cucumber.runtime.xstream.converters.SingleValueConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class composes all interesting parameter information into one object.
 */
public class ParameterType {
    public static final String DEFAULT_DELIMITER = ", ?";

    private final Type type;
    private final String dateFormat;
    private final String delimiter;
    private final SingleValueConverter singleValueConverter;

    public static List<ParameterType> fromMethod(Method method) {
        List<ParameterType> result = new ArrayList<ParameterType>();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            String dateFormat = null;
            String delimiter = DEFAULT_DELIMITER;
            SingleValueConverter singleValueConverter = null;
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof DateFormat) {
                    dateFormat = ((DateFormat) annotation).value();
                }
                if (annotation instanceof Delimiter) {
                    delimiter = ((Delimiter) annotation).value();
                }
                if (annotation instanceof Transform) {
                    try {
                        singleValueConverter = ((Transform) annotation).value().newInstance();
                    } catch (InstantiationException e) {
                        throw new CucumberException(e);
                    } catch (IllegalAccessException e) {
                        throw new CucumberException(e);
                    }
                }
            }
            result.add(new ParameterType(genericParameterTypes[i], dateFormat, delimiter, singleValueConverter));
        }
        return result;
    }

    public ParameterType(Type type, String dateFormat, String delimiter, SingleValueConverter singleValueConverter) {
        this.type = type;
        this.dateFormat = dateFormat;
        this.delimiter = delimiter;
        this.singleValueConverter = singleValueConverter;
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

    public Object convert(String value, LocalizedXStreams.LocalizedXStream xStream, Locale locale) {
        try {
            xStream.setDateFormat(dateFormat);
            SingleValueConverter converter;
            xStream.processAnnotations(getRawType(type));

            if (singleValueConverter != null) {
                converter = singleValueConverter;
            } else {
                if (List.class.isAssignableFrom(getRawType(type))) {
                    converter = getListConverter(type, xStream, locale);
                } else {
                    converter = getConverter(getRawType(type), xStream, locale);
                }
                if (converter == null) {
                    throw new CucumberException(String.format(
                            "Don't know how to convert \"%s\" into %s.\n" +
                                    "Try writing your own converter:\n" +
                                    "\n" +
                                    "@%s(%sConverter.class)\n" +
                                    "public class %s {}\n",
                            value,
                            getRawType(type).getName(),
                            XStreamConverter.class.getName(),
                            getRawType(type).getSimpleName(),
                            getRawType(type).getSimpleName()
                    ));
                }
            }
            return converter.fromString(value);
        } finally {
            xStream.unsetDateFormat();
        }
    }

    private SingleValueConverter getListConverter(Type type, LocalizedXStreams.LocalizedXStream xStream, Locale locale) {
        Class elementType = type instanceof ParameterizedType
                ? getRawType(((ParameterizedType)type).getActualTypeArguments()[0])
                : Object.class;

        SingleValueConverter elementConverter = getConverter(elementType, xStream, locale);
        if (elementConverter == null) {
            return null;
        } else {
            return new ListConverter(delimiter, elementConverter);
        }
    }

    private SingleValueConverter getConverter(Class<?> type, LocalizedXStreams.LocalizedXStream xStream, Locale locale) {
        if (type.isEnum()) {
            return new EnumConverter(locale, (Class<? extends Enum>) type);
        } else {
            return xStream.getSingleValueConverter(type);
        }
    }

    public String getDateFormat() {
        return dateFormat;
    }
}
