package io.cucumber.java;

import io.cucumber.core.api.Scenario;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.reflection.MethodFormat;
import io.cucumber.core.filter.TagPredicate;
import gherkin.pickles.PickleTag;
import io.cucumber.core.runtime.Invoker;

import java.lang.reflect.Method;
import java.util.Collection;

class JavaHookDefinition implements HookDefinition {

    private final Method method;
    private final long timeoutMillis;
    private final TagPredicate tagPredicate;
    private final int order;
    private final Lookup lookup;

    JavaHookDefinition(Method method, String tagExpression, int order, long timeoutMillis, Lookup lookup) {
        this.method = method;
        this.timeoutMillis = timeoutMillis;
        this.tagPredicate = new TagPredicate(tagExpression);
        this.order = order;
        this.lookup = lookup;
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

        Invoker.invoke(lookup.getInstance(method.getDeclaringClass()), method, timeoutMillis, args);
    }

    @Override
    public boolean matches(Collection<PickleTag> tags) {
        return tagPredicate.apply(tags);
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
