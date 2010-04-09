package cuke4duke.internal.java;

import cuke4duke.annotation.Pending;
import cuke4duke.spi.ExceptionFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvoker {
    private final ExceptionFactory exceptionFactory;

    public MethodInvoker(ExceptionFactory exceptionFactory) {
        this.exceptionFactory = exceptionFactory;
    }

    public Object invoke(Method method, Object target, Object[] javaArgs) throws Throwable {
        try {
            if(method.isAnnotationPresent(Pending.class)) {
                throw exceptionFactory.cucumberPending(method.getAnnotation(Pending.class).value());
            } else {
                return method.invoke(target, javaArgs);
            }
        } catch (IllegalArgumentException e) {
            String m = "Couldn't invokeWithArgs " + method.toGenericString() + " with " + cuke4duke.internal.Utils.join(javaArgs, ",");
            throw exceptionFactory.cucumberArityMismatchError(m);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
