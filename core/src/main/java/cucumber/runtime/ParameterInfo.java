package cucumber.runtime;

import cucumber.api.Transpose;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class composes all interesting parameter information into one object.
 */
public class ParameterInfo {
    private final Type type;
    private final boolean transposed;

    public static List<ParameterInfo> fromMethod(Method method) {
        List<ParameterInfo> result = new ArrayList<ParameterInfo>();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            boolean transposed = false;
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof Transpose) {
                    transposed = ((Transpose) annotation).value();
                }
            }
            result.add(new ParameterInfo(genericParameterTypes[i], transposed));
        }
        return result;
    }

    public static List<ParameterInfo> fromTypes(Type[] genericParameterTypes) {
        List<ParameterInfo> result = new ArrayList<ParameterInfo>();
        for (Type genericParameterType : genericParameterTypes) {
            result.add(new ParameterInfo(genericParameterType, false));
        }
        return result;
    }

    ParameterInfo(Type type) {
        this(type, false);
    }

    private ParameterInfo(Type type, boolean transposed) {
        this.type = type;
        this.transposed = transposed;
    }

    public Type getType() {
        return type;
    }

    public boolean isTransposed() {
        return transposed;
    }

    @Override
    public String toString() {
        return type.toString();
    }

}
