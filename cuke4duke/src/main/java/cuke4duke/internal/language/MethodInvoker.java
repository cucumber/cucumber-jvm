package cuke4duke.internal.language;

import cuke4duke.internal.StringConverter;
import cuke4duke.internal.JRuby;
import cuke4duke.Pending;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.exceptions.RaiseException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvoker {
    protected final Method method;

    public MethodInvoker(Method method) {
        this.method = method;
    }

    public void invoke(Object target, Class<?>[] types, RubyArray rubyArgs) throws Throwable {
        Object[] args = rubyArgs.toArray();
        Object[] javaArgs = new StringConverter().convert(types, args);
        try {
            if(method.isAnnotationPresent(Pending.class)) {
                raisePending();
            } else {
                method.invoke(target, javaArgs);
            }
        } catch (IllegalArgumentException e) {
            String m = "Couldn't invoke " + method.toGenericString() + " with " + cuke4duke.internal.Utils.join(args, ",");
            throw new IllegalArgumentException(m, e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private void raisePending() {
        String message = method.getAnnotation(Pending.class).value();
        RubyModule cucumber = JRuby.getRuntime().getModule("Cucumber");
        RubyClass pending = cucumber.getClass("Pending");
        throw new RaiseException(
                JRuby.getRuntime(),
                pending,
                message,
                true
        );
    }
}
