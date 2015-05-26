package cucumber.runtime.java;

import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;

import java.lang.reflect.Method;
import java.util.Collection;

public class JavaStepHookDefinition extends AbstractJavaHook<Step> {
    public JavaStepHookDefinition(Method method, int order, long timeoutMillis, ObjectFactory objectFactory) {
        super(method, order, timeoutMillis, objectFactory);
    }

    @Override
    public boolean matches(Collection<Tag> tags) {
        return true;
    }

    @Override
    public void execute(Step type) throws Throwable {
        super.execute(Step.class, type);
    }
}