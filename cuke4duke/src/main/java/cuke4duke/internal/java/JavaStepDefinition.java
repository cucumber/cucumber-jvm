package cuke4duke.internal.java;

import cuke4duke.internal.language.AbstractStepDefinition;
import cuke4duke.internal.JRuby;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.jvmclass.ObjectFactory;
import cuke4duke.internal.language.JdkPatternArgumentMatcher;
import cuke4duke.internal.language.MethodInvoker;
import cuke4duke.internal.language.StepArgument;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

public class JavaStepDefinition extends AbstractStepDefinition {
    private final Pattern regexp;
    private final MethodInvoker methodInvoker;
    private final ObjectFactory objectFactory;
    private final Method method;
    private final ClassLanguage classLanguage;

    public JavaStepDefinition(ObjectFactory objectFactory, Method method, Pattern regexp, ClassLanguage classLanguage) {
        this.objectFactory = objectFactory;
        this.method = method;
        this.classLanguage = classLanguage;
        methodInvoker = new MethodInvoker(method);
        this.regexp = regexp;
        classLanguage.availableStepDefinition(regexp_source(), file_colon_line());
    }

    public String regexp_source() {
        return regexp.pattern();
    }
    
    public List<StepArgument> arguments_from(String stepName) {
        return JdkPatternArgumentMatcher.argumentsFrom(regexp, stepName);
    }

    public String file_colon_line() {
        return method.toGenericString();
    }

    protected Class<?>[] getParameterTypes(Object[] args) {
        Class<?>[] types = method.getParameterTypes();
        if(types.length != args.length) {
            throw JRuby.cucumberArityMismatchError("Expected " + types.length + " arguments, got " + args.length);
        }
        return types;
    }

    public void invokeWithJavaArgs(Object[] args) throws Throwable {
        Object target = objectFactory.getComponent(method.getDeclaringClass());
        classLanguage.invokedStepDefinition(regexp_source(), file_colon_line());
        methodInvoker.invoke(target, args);
    }

}
