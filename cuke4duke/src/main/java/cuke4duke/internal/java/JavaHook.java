package cuke4duke.internal.java;

import cuke4duke.internal.JRuby;
import cuke4duke.internal.jvmclass.ObjectFactory;
import cuke4duke.internal.language.AbstractHook;
import cuke4duke.internal.language.MethodInvoker;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.Method;
import java.util.List;

public class JavaHook extends AbstractHook {
    private final MethodInvoker methodInvoker;
    private final Method method;
    private final ObjectFactory objectFactory;

    public JavaHook(List<String> tagNames, Method method, ObjectFactory objectFactory) {
        super(tagNames);
        this.method = method;
        this.objectFactory = objectFactory;
        this.methodInvoker = new MethodInvoker(method);
    }
    
    public JavaHook(Method method, ObjectFactory objectFactory) {
        super(null);
        this.method = method;
        this.objectFactory = objectFactory;
        this.methodInvoker = new MethodInvoker(method);
    }

    public void invoke(String location, IRubyObject scenario) throws Throwable {
        Object target = objectFactory.getComponent(method.getDeclaringClass());
        Object[] args = null;
        if(method.getParameterTypes().length == 1) {
            args = new Object[]{scenario};
        } else if(method.getParameterTypes().length > 1) {
            throw JRuby.cucumberArityMismatchError("Hooks must take 0 or 1 arguments. " + method);
        }
        methodInvoker.invoke(target, args);
    }
}
