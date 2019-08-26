package cucumber.runtime;

import cucumber.api.Delimiter;
import cucumber.api.Format;
import cucumber.api.Transform;
import cucumber.api.Transformer;
import cucumber.api.Transpose;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;
import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.runtime.xstream.LocalizedXStreams;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class composes all interesting parameter information into one object.
 */
public class ParameterInfo {
    public static final String DEFAULT_DELIMITER = ",\\s?";

    private final Type type;
    private final String format;
    private final String delimiter;
    private final boolean transposed;
    private final Transformer<?> transformer;

    public static List<ParameterInfo> fromMethod(Method method) {
        List<ParameterInfo> result = new ArrayList<ParameterInfo>();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            String format = null;
            String delimiter = DEFAULT_DELIMITER;
            boolean transposed = false;
            Transformer<?> transformer = null;
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof Format) {
                    format = ((Format) annotation).value();
                } else if (isAnnotatedWith(annotation, Format.class)) {
                    format = getAnnotationForAnnotation(annotation, Format.class).value();
                }

                if (annotation instanceof Delimiter) {
                    delimiter = ((Delimiter) annotation).value();
                } else if (isAnnotatedWith(annotation, Delimiter.class)) {
                    delimiter = getAnnotationForAnnotation(annotation, Delimiter.class).value();
                }
                if (annotation instanceof Transpose) {
                    transposed = ((Transpose) annotation).value();
                }
                if (annotation instanceof Transform) {
                    transformer = getTransformer(annotation);
                } else if (isAnnotatedWith(annotation, Transform.class)) {
                    transformer = getTransformer(getAnnotationForAnnotation(annotation, Transform.class));
                }
            }
            result.add(new ParameterInfo(genericParameterTypes[i], format, delimiter, transposed, transformer));
        }
        return result;
    }

    public static List<ParameterInfo> fromTypes(Type[] genericParameterTypes) {
        List<ParameterInfo> result = new ArrayList<ParameterInfo>();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            String format = null;
            String delimiter = DEFAULT_DELIMITER;
            boolean transposed = false;
            Transformer<?> transformer = null;
            result.add(new ParameterInfo(genericParameterTypes[i], format, delimiter, transposed, transformer));
        }
        return result;
    }

    private static boolean isAnnotatedWith(Annotation source, Class<? extends Annotation> requiredAnnotation) {
        return getAnnotationForAnnotation(source, requiredAnnotation) != null;
    }

    private static <T extends Annotation> T getAnnotationForAnnotation(Annotation source, Class<T> requiredAnnotation) {
        return source.annotationType().getAnnotation(requiredAnnotation);
    }

    private static Transformer<?> getTransformer(Annotation annotation) {
        try {
            return ((Transform) annotation).value().newInstance();
        } catch (InstantiationException e) {
            throw new CucumberException(e);
        } catch (IllegalAccessException e) {
            throw new CucumberException(e);
        }
    }

    public ParameterInfo(Type type, String format, String delimiter, Transformer transformer) {
        this(type, format, delimiter, false, transformer);
    }

    public ParameterInfo(Type type, String format, String delimiter, boolean transposed, Transformer transformer) {
        this.type = type;
        this.format = format;
        this.delimiter = delimiter;
        this.transposed = transposed;
        this.transformer = transformer;
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

    public boolean isTransposed() {
        return transposed;
    }

    @Override
    public String toString() {
        return type.toString();
    }

    public Object convert(String value, LocalizedXStreams.LocalizedXStream xStream) {
        try {
            xStream.setParameterInfo(this);
            SingleValueConverter converter;
            xStream.processAnnotations(getRawType());
            xStream.autodetectAnnotations(true); // Needed to unlock annotation processing

            if (transformer != null) {
                transformer.setParameterInfoAndLocale(this, xStream.getLocale());
                converter = transformer;
            } else {
                if (List.class.isAssignableFrom(getRawType())) {
                    converter = getListConverter(type, xStream);
                } else {
                    converter = xStream.getSingleValueConverter(getRawType());
                }
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
            return converter.fromString(value);
        } finally {
            xStream.unsetParameterInfo();
        }
    }

    private SingleValueConverter getListConverter(Type type, LocalizedXStreams.LocalizedXStream xStream) {
        Class elementType = type instanceof ParameterizedType
                ? getRawType(((ParameterizedType) type).getActualTypeArguments()[0])
                : Object.class;

        SingleValueConverter elementConverter = xStream.getSingleValueConverter(elementType);
        if (elementConverter == null) {
            return null;
        } else {
            return xStream.createListConverter(delimiter, elementConverter);
        }
    }

    public String getFormat() {
        return format;
    }
}
