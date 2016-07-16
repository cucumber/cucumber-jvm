package cucumber.runtime.java;

import cucumber.api.Scenario;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.CucumberException;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.MethodFormat;
import cucumber.runtime.Utils;
import cucumber.runtime.TagExpression;
import gherkin.pickles.PickleTag;

import java.lang.reflect.Method;
import java.util.Collection;

import static java.util.Arrays.asList;

class JavaHookDefinition implements HookDefinition {

    private final Method method;
    private final long timeoutMillis;
    private final TagExpression tagExpression;
    private final int order;
    private final ObjectFactory objectFactory;

    public JavaHookDefinition(Method method, String[] tagExpressions, int order, long timeoutMillis, ObjectFactory objectFactory) {
        this.method = method;
        this.timeoutMillis = timeoutMillis;
        this.tagExpression = new TagExpression(asList(tagExpressions));
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

        Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeoutMillis, args);
    }

    @Override
    public boolean matches(Collection<PickleTag> tags) {
        return tagExpression.evaluate(tags);
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
