package cuke4duke.internal.java;

import cuke4duke.internal.JRuby;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.language.MethodInvoker;
import cuke4duke.internal.language.StepDefinition;
import org.jruby.RubyArray;
import org.jruby.RubyRegexp;

import java.lang.reflect.Method;

public class JavaStepDefinition implements StepDefinition {
    private final RubyRegexp regexp;
    private final MethodInvoker methodInvoker;
    private final ClassLanguage classLanguage;
    private final Method method;

    public JavaStepDefinition(ClassLanguage classLanguage, Method method, String regexpString) {
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
