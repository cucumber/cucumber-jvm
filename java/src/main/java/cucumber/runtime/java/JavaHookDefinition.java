package cucumber.runtime.java;

import cucumber.runtime.HookDefinition;
import cucumber.runtime.MethodFormat;
import cucumber.runtime.ScenarioResult;
import cucumber.runtime.Utils;
import gherkin.TagExpression;
import gherkin.formatter.model.Tag;

import java.lang.reflect.Method;
import java.util.Collection;

import static java.util.Arrays.asList;

public class JavaHookDefinition implements HookDefinition {

    private final Method method;
    private final TagExpression tagExpression;
    private final int order;
    private final ObjectFactory objectFactory;

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
    public String getLocation(boolean detail) {
        MethodFormat format = detail ? MethodFormat.FULL : MethodFormat.SHORT;
        return format.format(method);
    }

    @Override
    public void execute(ScenarioResult scenarioResult) throws Throwable {
        Object[] args;
        if (method.getParameterTypes().length == 1) {
            args = new Object[]{scenarioResult};
        } else {
            args = new Object[0];
        }

        Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, args);
    }

    @Override
    public boolean matches(Collection<Tag> tags) {
        return tagExpression.eval(tags);
    }

    @Override
    public int getOrder() {
        return order;
    }

}
