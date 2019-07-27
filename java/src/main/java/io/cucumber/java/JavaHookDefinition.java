package io.cucumber.java;

import gherkin.pickles.PickleTag;
import io.cucumber.core.api.Scenario;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.filter.TagPredicate;
import io.cucumber.core.runtime.Invoker;

import java.lang.reflect.Method;
import java.util.Collection;

import static io.cucumber.java.InvalidMethodSignatureExceptionBuilder.builder;

final class JavaHookDefinition extends AbstractGlueDefinition implements HookDefinition {

    private final long timeoutMillis;
    private final TagPredicate tagPredicate;
    private final int order;
    private final Lookup lookup;

    JavaHookDefinition(Method method, String tagExpression, int order, long timeoutMillis, Lookup lookup) {
        super(requireValidMethod(method), lookup);
        this.timeoutMillis = timeoutMillis;
        this.tagPredicate = new TagPredicate(tagExpression);
        this.order = order;
        this.lookup = lookup;
    }

    Method getMethod() {
        return method;
    }

    private static Method requireValidMethod(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 1) {
            throw createInvalidSignatureException(method);
        }

        if (parameterTypes.length == 1) {
            if (!(Object.class.equals(parameterTypes[0]) || Scenario.class.equals(parameterTypes[0]))) {
                throw createInvalidSignatureException(method);
            }
        }

        return method;
    }

    private static CucumberException createInvalidSignatureException(Method method) {
        return builder(method)
            .addAnnotation(Before.class)
            .addAnnotation(After.class)
            .addAnnotation(BeforeStep.class)
            .addAnnotation(AfterStep.class)
            .addSignature("public void before_or_after(Scenario scenario)")
            .addSignature("public void before_or_after()")
            .build();
    }

    @Override
    public void execute(Scenario scenario) throws Throwable {
        Object[] args;
        if (method.getParameterTypes().length == 1) {
            args = new Object[]{scenario};
        } else {
            args = new Object[0];
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
}
