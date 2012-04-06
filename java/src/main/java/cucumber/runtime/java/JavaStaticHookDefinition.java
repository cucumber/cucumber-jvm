package cucumber.runtime.java;

import static java.util.Arrays.asList;
import gherkin.TagExpression;
import gherkin.formatter.model.Tag;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import cucumber.runtime.CucumberException;
import cucumber.runtime.StaticHookDefinition;

public class JavaStaticHookDefinition implements StaticHookDefinition {

    private final Method method;
    private final TagExpression tagExpression;
    private final int order;

    public JavaStaticHookDefinition(Method method, String[] tagExpressions, int order) {
        this.method = method;
        tagExpression = new TagExpression(asList(tagExpressions));
        this.order = order;
    }

    Method getMethod() {
        return method;
    }

    @Override
    public void execute() throws Throwable {
        Object[] args = new Object[0];

        try {
            method.invoke(null, args);
        } catch (IllegalArgumentException e) {
            throw new CucumberException("Can't invoke " + new MethodFormat().format(method));
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
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
