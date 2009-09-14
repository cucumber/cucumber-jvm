package cuke4duke.internal.java;

import cuke4duke.internal.JRuby;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.language.AbstractHook;
import cuke4duke.internal.language.MethodInvoker;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.Method;
import java.util.List;

public class JavaHook extends AbstractHook {
    private final MethodInvoker methodInvoker;
    private final Method method;
    private final ClassLanguage classLanguage;

    public JavaHook(List<String> tagNames, Method method, ClassLanguage classLanguage) {
        super(tagNames);
        this.method = method;
        this.classLanguage = classLanguage;
        this.methodInvoker = new MethodInvoker(method);
    }

    public void invoke(String location, IRubyObject scenario) throws Throwable {
        Object target = classLanguage.getTarget(method.getDeclaringClass());
        RubyArray args = RubyArray.newArray(JRuby.getRuntime());
        if(method.getParameterTypes().length == 1) {
            args.append(scenario);
        } else if(method.getParameterTypes().length > 1) {
            throw new RuntimeException("Hooks must take 0 or 1 arguments. " + method);
        }
        methodInvoker.invoke(target, method.getParameterTypes(), args);
    }
}
