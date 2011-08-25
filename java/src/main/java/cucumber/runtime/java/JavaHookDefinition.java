package cucumber.runtime.java;

import cucumber.runtime.CucumberException;
import cucumber.runtime.HookDefinition;
import gherkin.TagExpression;

import java.lang.reflect.Method;
import java.util.Collection;

import static java.util.Arrays.asList;

public class JavaHookDefinition implements HookDefinition {

    private final ObjectFactory objectFactory;
    private final Method method;
    private final TagExpression tagExpression;

    public JavaHookDefinition(Method method, String[] tagExpressions, ObjectFactory objectFactory) {
        this.method = method;
        tagExpression = new TagExpression(asList(tagExpressions));
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

    @Override
    public boolean matches(Collection<String> tags) {
        return tagExpression.eval(tags);
    }

}
