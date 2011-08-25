package cucumber.runtime.java;

import java.lang.reflect.Method;

import cucumber.runtime.CucumberException;
import cucumber.runtime.HookDefinition;

public class JavaHookDefinition implements HookDefinition {

    private final ObjectFactory objectFactory;
    private final Method method;

    public JavaHookDefinition(Method method, ObjectFactory objectFactory) {
        this.method = method;
        this.objectFactory = objectFactory;
    }

    Method getMethod() {
        return method;
    }

    @Override
    public void execute() throws Throwable {
        Object target = objectFactory.getInstance(method.getDeclaringClass());
        try {
            method.invoke(target);
        } catch (IllegalArgumentException e) {
            throw new CucumberException("Can't invoke "
                    + new MethodFormat().format(method));
        }
    }

}
