package cuke4duke.internal.java;

import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.language.Transformable;

import java.lang.reflect.Method;
import java.util.Locale;

public class JavaTransform implements Transformable {

    private final ClassLanguage classLanguage;
    private final Method method;

    public JavaTransform(ClassLanguage classLanguage, Method method) {
        this.classLanguage = classLanguage;
        this.method = method;
    }

    @SuppressWarnings("unchecked")
    public <T> T transform(Object arg, Locale locale) throws Throwable {
        return (T) classLanguage.invoke(method, new Object[]{arg}, locale);
    }

}
