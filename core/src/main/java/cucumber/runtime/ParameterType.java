package cucumber.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This class composes all interesting parameter information into one object.
 */
public class ParameterType {
    private final Class<?> clazz;
    private final Type genericType;
    private final Annotation[] annotations;

    public ParameterType(Class<?> clazz, Type genericType, Annotation[] annotations) {
        this.clazz = clazz;
        this.genericType = genericType;
        this.annotations = annotations;
    }

    public Class<?> getParameterClass() {
        return clazz;
    }

    public Type[] getActualTypeArguments() {
        if (genericType != null && genericType instanceof ParameterizedType) {
            Type[] parameters = ((ParameterizedType) genericType).getActualTypeArguments();
            return parameters;
        } else {
            return null;
        }
    }
}
