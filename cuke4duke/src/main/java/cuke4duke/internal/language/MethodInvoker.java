package cuke4duke.internal.language;

import cuke4duke.internal.StringConverter;
import org.jruby.RubyArray;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvoker {
    protected final Method method;

    public MethodInvoker(Method method) {
        this.method = method;
    }

    public void invoke(Object target, Class<?>[] types, RubyArray rubyArgs) throws Throwable {
        Object[] args = rubyArgs.toArray();
        Object[] converted = new StringConverter().convert(types, args);
        try {
            method.invoke(target, converted);
        } catch (IllegalArgumentException e) {
            String m = "Couldn't invoke " + method.toGenericString() + " with " + cuke4duke.internal.Utils.join(args, ",");
            throw new IllegalArgumentException(m, e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
