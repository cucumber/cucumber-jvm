package cucumber.runtime;

import cucumber.api.Format;
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
    private final String format;
    private final boolean transposed;

    public static List<ParameterInfo> fromMethod(Method method) {
        List<ParameterInfo> result = new ArrayList<ParameterInfo>();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            String format = null;
            boolean transposed = false;
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof Format) {
                    format = ((Format) annotation).value();
                } else if (isAnnotatedWith(annotation, Format.class)) {
                    format = getAnnotationForAnnotation(annotation, Format.class).value();
                }
                if (annotation instanceof Transpose) {
                    transposed = ((Transpose) annotation).value();
                }
            }
            result.add(new ParameterInfo(genericParameterTypes[i], format, transposed));
        }
        return result;
    }

    public static List<ParameterInfo> fromTypes(Type[] genericParameterTypes) {
        List<ParameterInfo> result = new ArrayList<ParameterInfo>();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            String format = null;
            boolean transposed = false;
            result.add(new ParameterInfo(genericParameterTypes[i], format, transposed));
        }
        return result;
    }

    private static boolean isAnnotatedWith(Annotation source, Class<? extends Annotation> requiredAnnotation) {
        return getAnnotationForAnnotation(source, requiredAnnotation) != null;
    }

    private static <T extends Annotation> T getAnnotationForAnnotation(Annotation source, Class<T> requiredAnnotation) {
        return source.annotationType().getAnnotation(requiredAnnotation);
    }

    public ParameterInfo(Type type, String format) {
        this(type, format, false);
    }

    public ParameterInfo(Type type, String format, boolean transposed) {
        this.type = type;
        this.format = format;
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

    public String getFormat() {
        return format;
    }
}
