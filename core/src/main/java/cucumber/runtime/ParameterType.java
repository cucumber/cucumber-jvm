package cucumber.runtime;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
}
