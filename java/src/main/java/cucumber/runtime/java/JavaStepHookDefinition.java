package cucumber.runtime.java;

import cucumber.runtime.CucumberException;
import cucumber.runtime.MethodFormat;
import cucumber.runtime.StepHookDefinition;
import cucumber.runtime.Utils;
import gherkin.formatter.model.Step;

import java.lang.reflect.Method;

public class JavaStepHookDefinition implements StepHookDefinition {
    private final Method method;
    private final long timeoutMillis;
    private final int order;
    private final ObjectFactory objectFactory;

    public JavaStepHookDefinition(Method method, int order, long timeoutMillis, ObjectFactory objectFactory) {
        this.method = method;
        this.timeoutMillis = timeoutMillis;
        this.order = order;
        this.objectFactory = objectFactory;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public void execute(Step step) throws Throwable {
        Object[] args;
        switch (method.getParameterTypes().length) {
            case 0:
                args = new Object[0];
                break;
            case 1:
                if (!Step.class.equals(method.getParameterTypes()[0])) {
                    throw new CucumberException("When a step hook declares an argument it must be of type " + Step.class.getName() + ". " + method.toString());
                }
                args = new Object[]{step};
                break;
            default:
                throw new CucumberException("Step hooks must declare 0 or 1 arguments. " + method.toString());
        }

        Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeoutMillis, args);
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
}