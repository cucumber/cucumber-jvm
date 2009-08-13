package cuke4duke.internal.java;

import cuke4duke.internal.language.AbstractHook;
import cuke4duke.internal.language.MethodInvoker;
import org.jruby.RubyArray;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;
import java.lang.reflect.Method;

public class JavaHook extends AbstractHook {
    private final MethodInvoker methodInvoker;
    private final Method method;
    private final JavaLanguage javaLanguage;

    public JavaHook(List<String> tagNames, Method method, JavaLanguage javaLanguage) {
        super(tagNames);
        this.method = method;
        this.javaLanguage = javaLanguage;
        this.methodInvoker = new MethodInvoker(method);
    }

    public void invoke(String location, IRubyObject scenario) throws Throwable {
        Object target = javaLanguage.getTarget(method.getDeclaringClass());
        RubyArray args = RubyArray.newArray(Ruby.getGlobalRuntime());
        args.append(scenario);
        methodInvoker.invoke(target, method.getParameterTypes(), args);
    }
}
