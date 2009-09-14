package cuke4duke.internal.annotation;

import cuke4duke.internal.language.MethodInvoker;
import cuke4duke.internal.language.StepDefinition;
import cuke4duke.internal.JRuby;
import cuke4duke.internal.jvmclass.ClassLanguage;
import org.jruby.RubyArray;
import org.jruby.RubyRegexp;

import java.lang.reflect.Method;

public class AnnotationStepDefinition implements StepDefinition {
    private final RubyRegexp regexp;
    private final MethodInvoker methodInvoker;
    private final ClassLanguage classLanguage;
    private final Method method;

    public AnnotationStepDefinition(ClassLanguage classLanguage, Method method, String regexpString) {
        this.classLanguage = classLanguage;
        this.method = method; 
        methodInvoker = new MethodInvoker(method);
        this.regexp = RubyRegexp.newRegexp(JRuby.getRuntime(), regexpString, RubyRegexp.RE_OPTION_LONGEST);
    }

    public RubyRegexp regexp() {
        return regexp;
    }

    public String file_colon_line() {
        return method.toGenericString();
    }

    public void invoke(RubyArray rubyArgs) throws Throwable {
        Object target = classLanguage.getTarget(method.getDeclaringClass());
        Class<?>[] types = method.getParameterTypes();
        methodInvoker.invoke(target, types, rubyArgs);
    }

}
