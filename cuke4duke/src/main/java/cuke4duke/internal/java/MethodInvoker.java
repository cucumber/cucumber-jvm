package cuke4duke.internal.java;

import cuke4duke.Pending;
import cuke4duke.internal.JRuby;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvoker {
    public Object invoke(Method method, Object target, Object[] javaArgs) throws Throwable {
        try {
            if(method.isAnnotationPresent(Pending.class)) {
                throw JRuby.cucumberPending(method.getAnnotation(Pending.class).value());
            } else {
                return method.invoke(target, javaArgs);
            }
        } catch (IllegalArgumentException e) {
            String m = "Couldn't invokeWithArgs " + method.toGenericString() + " with " + cuke4duke.internal.Utils.join(javaArgs, ",");
            throw JRuby.cucumberArityMismatchError(m);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
