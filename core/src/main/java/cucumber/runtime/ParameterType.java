package cucumber.runtime;

import cucumber.DateFormat;
import cucumber.api.Transform;
import cucumber.runtime.converters.EnumConverter;
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
    private final Type type;
    private final String dateFormat;
    private final SingleValueConverter singleValueConverter;

    public static List<ParameterType> fromMethod(Method method) {
        List<ParameterType> result = new ArrayList<ParameterType>();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            String dateFormat = null;
            SingleValueConverter singleValueConverter = null;
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof DateFormat) {
                    dateFormat = ((DateFormat) annotation).value();
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
            result.add(new ParameterType(genericParameterTypes[i], dateFormat, singleValueConverter));
        }
        return result;
    }

    public ParameterType(Type type, String dateFormat, SingleValueConverter singleValueConverter) {
        this.type = type;
        this.dateFormat = dateFormat;
        this.singleValueConverter = singleValueConverter;
    }

    public Class<?> getRawType() {
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
            xStream.processAnnotations(getRawType());

            if (singleValueConverter != null) {
                converter = singleValueConverter;
            } else {
                if (getRawType().isEnum()) {
                    converter = new EnumConverter(locale, (Class<? extends Enum>) getRawType());
                } else {
                    converter = xStream.getSingleValueConverter(getRawType());
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
            }
            return converter.fromString(value);
        } finally {
            xStream.unsetDateFormat();
        }
    }

    public String getDateFormat() {
        return dateFormat;
    }
}
