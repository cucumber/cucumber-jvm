package cuke4duke.internal.language;

import cuke4duke.Pending;
import cuke4duke.internal.JRuby;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvoker {
    protected final Method method;

    public MethodInvoker(Method method) {
        this.method = method;
    }

    public Object invoke(Object target, Object[] javaArgs) throws Throwable {
        try {
            if(method.isAnnotationPresent(Pending.class)) {
                throw JRuby.cucumberPending(method.getAnnotation(Pending.class).value());
            } else {
                return method.invoke(target, javaArgs);
            }
        } catch (IllegalArgumentException e) {
            String m = "Couldn't invokeWithJavaArgs " + method.toGenericString() + " with " + cuke4duke.internal.Utils.join(javaArgs, ",");
            throw JRuby.cucumberArityMismatchError(m);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
