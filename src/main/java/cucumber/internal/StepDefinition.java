package cucumber.internal;

import org.jruby.RubyArray;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StepDefinition {
    private final String regexpString;
    public final Object target;
    private final Method method;

    public StepDefinition(String regexpString, Object target, Method method) {
        this.regexpString = regexpString;
        this.target = target;
        this.method = method;
    }

    public String getRegexpString() {
        return regexpString;
    }

    public String file_colon_line() {
        return method.toGenericString();
    }

    public void invokeOnTarget(RubyArray args) throws Throwable {
        invokeOnTarget(args.toArray());
    }

    void invokeOnTarget(Object[] args) throws Throwable {
        try {
            Object[] convArgs = new Object[args.length];
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                Class clazz = method.getParameterTypes()[i];
                if (clazz.equals(Integer.TYPE)) {
                    convArgs[i] = Integer.valueOf((String) args[i]);
                } else {
                    convArgs[i] = args[i];
                }
            }
            method.invoke(target, convArgs);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

    }
}
