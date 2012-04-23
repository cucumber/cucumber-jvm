package cucumber.runtime;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.ConverterMatcher;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.DateFormat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class composes all interesting parameter information into one object.
 */
public class ParameterType {
    private final Type type;
    private final String dateFormat;

    public ParameterType(Type type, String dateFormat) {
        this.type = type;
        this.dateFormat = dateFormat;
    }

    public Class<?> getParameterClass() {
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            return (Class<?>) type;
        }
    }

    public SingleValueConverter getSingleValueConverter() {
        XStreamConverter annotation = getParameterClass().getAnnotation(XStreamConverter.class);
        if (annotation != null) {
            try {
                ConverterMatcher converterMatcher = annotation.value().newInstance();
                if (converterMatcher instanceof SingleValueConverter) {
                    return (SingleValueConverter) converterMatcher;
                } else {
                    throw new CucumberException(String.format("%s must implement %s", annotation.value(), SingleValueConverter.class));
                }
            } catch (InstantiationException e) {
                throw new CucumberException(e);
            } catch (IllegalAccessException e) {
                throw new CucumberException(e);
            }
        } else {
            return null;
        }
    }

    public Type[] getActualTypeArguments() {
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments();
        } else {
            return null;
        }
    }

    public String getDateFormat() {
        return dateFormat;
    }

    @Override
    public String toString() {
        return type.toString();
    }

    public static List<ParameterType> fromMethod(Method method) {
        List<ParameterType> result = new ArrayList<ParameterType>();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            String dateFormat = null;
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof DateFormat) {
                    dateFormat = ((DateFormat) annotation).value();
                    break;
                }
            }
            result.add(new ParameterType(genericParameterTypes[i], dateFormat));
        }
        return result;
    }
}
