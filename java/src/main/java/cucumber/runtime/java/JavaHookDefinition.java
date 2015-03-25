package cucumber.runtime.java;

import cucumber.api.Scenario;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Utils;
import gherkin.TagExpression;
import gherkin.formatter.model.Tag;

import java.lang.reflect.Method;
import java.util.Collection;

import static java.util.Arrays.asList;

class JavaHookDefinition extends AbstractJavaHook<Scenario> {
    protected final TagExpression tagExpression;

    public JavaHookDefinition(Method method, String[] tagExpressions, int order, long timeoutMillis, ObjectFactory objectFactory) {
        super(method, order, timeoutMillis, objectFactory);
        this.tagExpression = new TagExpression(asList(tagExpressions));
    }

    @Override
    public boolean matches(Collection<Tag> tags) {
        return tagExpression.evaluate(tags);
    }

    @Override
    public void execute(Scenario type) throws Throwable {
        super.execute(Scenario.class, type);
    }
}