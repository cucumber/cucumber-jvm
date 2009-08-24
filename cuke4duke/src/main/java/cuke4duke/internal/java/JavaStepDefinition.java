package cuke4duke.internal.java;

import cuke4duke.internal.language.MethodInvoker;
import cuke4duke.internal.language.StepDefinition;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyRegexp;

import java.lang.reflect.Method;

public class JavaStepDefinition implements StepDefinition {
    private final RubyRegexp regexp;
    private final JavaLanguage javaLanguage;
    private final MethodInvoker methodInvoker;
    private final Method method;

    public JavaStepDefinition(JavaLanguage javaLanguage, Method method, String regexpString) {
        this.method = method; 
        methodInvoker = new MethodInvoker(method);
        this.javaLanguage = javaLanguage;
        this.regexp = RubyRegexp.newRegexp(Ruby.getGlobalRuntime(), regexpString, RubyRegexp.RE_OPTION_LONGEST);
    }

    public RubyRegexp regexp() {
        return regexp;
    }

    public String file_colon_line() {
        return method.toGenericString();
    }

    public void invoke(RubyArray rubyArgs) throws Throwable {
        Object target = javaLanguage.getTarget(method.getDeclaringClass());
        Class<?>[] types = method.getParameterTypes();
        methodInvoker.invoke(target, types, rubyArgs);
    }

}
