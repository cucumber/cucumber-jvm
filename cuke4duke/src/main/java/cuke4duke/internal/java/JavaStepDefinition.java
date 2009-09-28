package cuke4duke.internal.java;

import cuke4duke.internal.jvmclass.ObjectFactory;
import cuke4duke.internal.language.MethodInvoker;
import cuke4duke.internal.language.StepDefinition;
import cuke4duke.internal.language.StepArgument;
import cuke4duke.internal.language.JdkPatternArgumentMatcher;
import org.jruby.RubyArray;

import java.lang.reflect.Method;
import java.util.regex.Pattern;
import java.util.List;

public class JavaStepDefinition implements StepDefinition {
    private final Pattern regexp;
    private final MethodInvoker methodInvoker;
    private final ObjectFactory objectFactory;
    private final Method method;

    public JavaStepDefinition(ObjectFactory objectFactory, Method method, Pattern regexp) {
        this.objectFactory = objectFactory;
        this.method = method; 
        methodInvoker = new MethodInvoker(method);
        this.regexp = regexp;
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

    public void invoke(RubyArray rubyArgs) throws Throwable {
        Object target = objectFactory.getComponent(method.getDeclaringClass());
        Class<?>[] types = method.getParameterTypes();
        methodInvoker.invoke(target, types, rubyArgs);
    }

}
