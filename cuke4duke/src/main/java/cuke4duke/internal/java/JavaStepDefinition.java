package cuke4duke.internal.java;

import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.language.AbstractStepDefinition;
import cuke4duke.internal.language.JdkPatternArgumentMatcher;
import cuke4duke.internal.language.StepArgument;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

public class JavaStepDefinition extends AbstractStepDefinition {
    private final ClassLanguage classLanguage;
    private final Method method;
    private final Pattern regexp;
    private final MethodFormat methodFormat;

    public JavaStepDefinition(ClassLanguage programmingLanguage, Method method, Pattern regexp, MethodFormat methodFormat) throws Throwable {
        super(programmingLanguage);
        this.classLanguage = programmingLanguage;
        this.method = method;
        this.regexp = regexp;
        this.methodFormat = methodFormat;
        register();
    }

    public String regexp_source() {
        return regexp.pattern();
    }
    
    public List<StepArgument> arguments_from(String stepName) throws UnsupportedEncodingException {
        return JdkPatternArgumentMatcher.argumentsFrom(regexp, stepName);
    }

    public String file_colon_line() {
        return methodFormat.format(method);
    }

    public Object invokeWithArgs(Object[] args) throws Throwable {
        return classLanguage.invoke(method, args);
    }

}
