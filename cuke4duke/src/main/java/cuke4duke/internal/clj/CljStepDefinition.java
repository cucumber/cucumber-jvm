package cuke4duke.internal.clj;

import clojure.lang.AFunction;
import cuke4duke.internal.language.AbstractStepDefinition;
import cuke4duke.internal.Utils;
import cuke4duke.internal.language.JdkPatternArgumentMatcher;
import cuke4duke.internal.language.StepArgument;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

public class CljStepDefinition extends AbstractStepDefinition {
    private final Pattern regexp;
    private final AFunction closure;

    public CljStepDefinition(Pattern regexp, AFunction closure) {
        this.regexp = regexp;
        this.closure = closure;
    }

    public String regexp_source() {
        return regexp.pattern();
    }

    public String file_colon_line() {
        return regexp_source();
    }

    protected Class<?>[] getParameterTypes(Object[] args) {
        return Utils.objectClassArray(args.length);
    }

    public void invokeWithJavaArgs(Object[] javaArgs) throws Throwable {
        Method functionInvoke = lookupInvokeMethod(javaArgs);
        try {
            functionInvoke.invoke(closure, javaArgs);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public List<StepArgument> arguments_from(String stepName) {
        return JdkPatternArgumentMatcher.argumentsFrom(regexp, stepName);
    }

    // Clojure's AFunction.invoke doesn't take varargs :-/
    private Method lookupInvokeMethod(Object[] args) throws NoSuchMethodException {
        return AFunction.class.getMethod("invoke", getParameterTypes(args));
    }
}
