package cucumber.runtime.java;

import cucumber.api.Transpose;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class composes all interesting parameter information into one object.
 */
class ParameterInfo {
    private final Type type;
    private final boolean transposed;

    static List<ParameterInfo> fromMethod(Method method) {
        List<ParameterInfo> result = new ArrayList<ParameterInfo>();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            boolean transposed = false;
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof Transpose ) {
                    transposed = ((Transpose) annotation).value();
                } else if (annotation instanceof io.cucumber.java.Transpose) {
                    transposed = ((io.cucumber.java.Transpose) annotation).value();
                }
            }
            result.add(new ParameterInfo(genericParameterTypes[i], transposed));
        }
        return result;
    }

    private ParameterInfo(Type type, boolean transposed) {
        this.type = type;
        this.transposed = transposed;
    }

    Type getType() {
        return type;
    }

    boolean isTransposed() {
        return transposed;
    }

    @Override
    public String toString() {
        return type.toString();
    }

}
