package cuke4duke.internal.java;

import java.lang.reflect.Method;

import cuke4duke.internal.jvmclass.ObjectFactory;
import cuke4duke.internal.language.MethodInvoker;
import cuke4duke.internal.language.Transformable;

public class JavaTransform implements Transformable {

    private final Method method;
    private final ObjectFactory objectFactory;
    private final MethodInvoker methodInvoker;

    public JavaTransform(Method method, ObjectFactory objectFactory) {
        this.method = method;
        this.objectFactory = objectFactory;
        this.methodInvoker = new MethodInvoker(method);
    }

    public Class<?> transform(Class<?> returnType, Object arg) {
        String argument = String.valueOf(arg);
        Object target = objectFactory.getComponent(method.getDeclaringClass());
        return methodInvoker.invoke(target, new Object[]{argument});
    }

}
