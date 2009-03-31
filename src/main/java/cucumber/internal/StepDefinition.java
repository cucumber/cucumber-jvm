package cucumber.internal;

import org.jruby.RubyArray;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StepDefinition {
    private final String regexpString;
    private final Object target;
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
            Object[] convertedArgs = conertArgs(args);
            method.invoke(target, convertedArgs);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

    }

    private Object[] conertArgs(Object[] args) {
        Object[] convertedArgs = new Object[args.length];
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Class clazz = method.getParameterTypes()[i];
            convertedArgs[i] = convertArg(clazz, args[i]);
        }
        return convertedArgs;
    }

    private Object convertArg(Class clazz, Object arg) {
        if (clazz.equals(Integer.TYPE)) {
            return Integer.valueOf((String) arg);
        } else {
            return arg;
        }
    }
}
