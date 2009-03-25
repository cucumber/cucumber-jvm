package cucumber.internal;

import org.jruby.RubyArray;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

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
            method.invoke(target, args);
        } catch(InvocationTargetException e) {
            throw e.getTargetException();
        }

    }
}
