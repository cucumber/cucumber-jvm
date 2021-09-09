package io.cucumber.java;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.TypeResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class composes all interesting parameter information into one object.
 */
class JavaParameterInfo implements ParameterInfo {

    private final Type type;
    private final boolean transposed;

    private JavaParameterInfo(Type type, boolean transposed) {
        this.type = type;
        this.transposed = transposed;
    }

    static List<ParameterInfo> fromMethod(Method method) {
        List<ParameterInfo> result = new ArrayList<>();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            boolean transposed = false;
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof Transpose) {
                    transposed = ((Transpose) annotation).value();
                }
            }
            result.add(new JavaParameterInfo(genericParameterTypes[i], transposed));
        }
        return result;
    }

    public Type getType() {
        return type;
    }

    public boolean isTransposed() {
        return transposed;
    }

    @Override
    public TypeResolver getTypeResolver() {
        return () -> type;
    }

}
