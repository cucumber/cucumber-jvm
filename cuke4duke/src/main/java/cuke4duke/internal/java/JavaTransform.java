package cuke4duke.internal.java;

import cuke4duke.internal.jvmclass.ObjectFactory;
import cuke4duke.internal.language.Transformable;

import java.lang.reflect.Method;

public class JavaTransform implements Transformable {

    private final Method method;
    private final ObjectFactory objectFactory;
    private final MethodInvoker methodInvoker;

    public JavaTransform(Method method, ObjectFactory objectFactory) {
        this.method = method;
        this.objectFactory = objectFactory;
        this.methodInvoker = new MethodInvoker(method);
    }

    @SuppressWarnings("unchecked")
    public <T> T transform(Class<T> returnType, Object arg) throws Throwable {
        String argument = String.valueOf(arg);
        Object target = objectFactory.getComponent((Class<?>) method.getDeclaringClass());
        return (T) (methodInvoker.invoke(target, new Object[] { argument }));
    }

}
