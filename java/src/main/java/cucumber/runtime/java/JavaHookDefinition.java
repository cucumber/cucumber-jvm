package cucumber.runtime.java;

import cucumber.api.Scenario;
import cucumber.runtime.CucumberException;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.MethodFormat;
import cucumber.runtime.Utils;

import java.lang.reflect.Method;

class JavaHookDefinition implements HookDefinition {

    private final Method method;
    private final int timeout;
    private final String tagExpression;
    private final int order;
    private final ObjectFactory objectFactory;

    public JavaHookDefinition(Method method, String tagExpression, int order, int timeout, ObjectFactory objectFactory) {
        this.method = method;
        this.timeout = timeout;
        this.tagExpression = tagExpression;
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
    public void execute(Scenario scenario) throws Throwable {
        Object[] args;
        switch (method.getParameterTypes().length) {
            case 0:
                args = new Object[0];
                break;
            case 1:
                if (!Scenario.class.equals(method.getParameterTypes()[0])) {
                    throw new CucumberException("When a hook declares an argument it must be of type " + Scenario.class.getName() + ". " + method.toString());
                }
                args = new Object[]{scenario};
                break;
            default:
                throw new CucumberException("Hooks must declare 0 or 1 arguments. " + method.toString());
        }

        Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeout, args);
    }

    @Override
    public String getTagExpression() {
        return tagExpression;
    }

    @Override
    public int getOrder() {
        return order;
    }

}
