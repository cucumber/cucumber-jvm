package cucumber.runtime.java;

import cucumber.runtime.CucumberException;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.ScenarioResult;
import gherkin.TagExpression;

import java.lang.reflect.Method;
import java.util.Collection;

import static java.util.Arrays.asList;

public class JavaHookDefinition implements HookDefinition {

    private final ObjectFactory objectFactory;
    private final Method method;
    private final int order;
    private final TagExpression tagExpression;

    public JavaHookDefinition(Method method, String[] tagExpressions, int order, ObjectFactory objectFactory) {
        this.method = method;
        tagExpression = new TagExpression(asList(tagExpressions));
        this.order = order;
        this.objectFactory = objectFactory;
    }

    Method getMethod() {
        return method;
    }

    @Override
    public void execute(ScenarioResult scenarioResult) throws Throwable {
        Object target = objectFactory.getInstance(method.getDeclaringClass());
        Object[] args;
        if(method.getParameterTypes().length == 1) {
            args = new Object[]{scenarioResult};
        } else {
            args = new Object[0];
        }
        try {
            method.invoke(target, args);
        } catch (IllegalArgumentException e) {
            throw new CucumberException("Can't invoke "
                    + new MethodFormat().format(method));
        }
    }

    @Override
    public boolean matches(Collection<String> tags) {
        return tagExpression.eval(tags);
    }

    @Override
    public int getOrder() {
        return order;
    }

}
