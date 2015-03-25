package cucumber.runtime.java;

import cucumber.runtime.CucumberException;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.MethodFormat;
import cucumber.runtime.Utils;

import java.lang.reflect.Method;

public abstract class AbstractJavaHook<T> implements HookDefinition<T> {
    protected final Method method;
    protected final long timeoutMillis;
    protected final int order;
    protected final ObjectFactory objectFactory;

    protected AbstractJavaHook(Method method, int order, long timeoutMillis, ObjectFactory objectFactory) {
        this.method = method;
        this.timeoutMillis = timeoutMillis;
        this.order = order;
        this.objectFactory = objectFactory;
    }

    Method getMethod() {
        return method;
    }

    @Override
    public String getLocation(boolean detail) {
        MethodFormat format = detail ? MethodFormat.FULL : MethodFormat.SHORT;
        return format.format(method);
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public boolean isScenarioScoped() {
        return false;
    }

    public void execute(Class<?> expType, T type) throws Throwable {
        Object[] args;
        switch (method.getParameterTypes().length) {
            case 0:
                args = new Object[0];
                break;
            case 1:
                checkHookArg(expType);
                args = new Object[]{type};
                break;
            default:
                throw new CucumberException("Hooks must declare 0 or 1 arguments. " + method.toString());
        }
        Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeoutMillis, args);
    }

    protected void checkHookArg(Class<?> expectedType) {
        if (!method.getParameterTypes()[0].isAssignableFrom(expectedType)) {
            throw new CucumberException("When a hook declares an argument it must be of type " + expectedType.getName() + ". " + method.toString());
        }
    }
}