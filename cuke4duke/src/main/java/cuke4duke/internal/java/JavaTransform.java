package cuke4duke.internal.java;

import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.language.Transformable;

import java.lang.reflect.Method;

public class JavaTransform implements Transformable {

    private final ClassLanguage classLanguage;
    private final Method method;

    public JavaTransform(ClassLanguage classLanguage, Method method) {
        this.classLanguage = classLanguage;
        this.method = method;
    }

    @SuppressWarnings("unchecked")
    public <T> T transform(Object arg) throws Throwable {
        return (T) classLanguage.invoke(method, new Object[]{arg});
    }

}
