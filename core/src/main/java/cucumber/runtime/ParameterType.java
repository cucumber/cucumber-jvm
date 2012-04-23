package cucumber.runtime;

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
