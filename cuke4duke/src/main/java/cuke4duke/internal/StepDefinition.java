package cuke4duke.internal;

import org.jruby.RubyArray;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StepDefinition {
    private final String regexpString;
    private final Object stepObject;
    private final Method method;

    public StepDefinition(String regexpString, Object stepObject, Method method) {
        this.regexpString = regexpString;
        this.stepObject = stepObject;
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
            Object[] convertedArgs = convertArgs(args);
            method.invoke(stepObject, convertedArgs);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private Object[] convertArgs(Object[] args) {
        if (method.getParameterTypes().length != args.length) {
            throw new RuntimeException("The method " + method + " was called with " + args.length + " arguments.");
        }
        Object[] convertedArgs = new Object[args.length];
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Class<?> clazz = method.getParameterTypes()[i];
            convertedArgs[i] = convertArg(clazz, args[i]);
        }
        return convertedArgs;
    }

    private Object convertArg(Class<?> clazz, Object arg) {
        if (clazz.equals(Integer.TYPE)) {
            return Integer.valueOf((String) arg);
        } else if (clazz.equals(Long.TYPE)) {
            return Long.valueOf((String) arg);
        } else {
            return arg;
        }
    }
}
