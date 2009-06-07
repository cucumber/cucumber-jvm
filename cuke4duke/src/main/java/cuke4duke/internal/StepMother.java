package cuke4duke.internal;

import cuke4duke.Given;
import cuke4duke.Then;
import cuke4duke.When;
import cuke4duke.Before;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jruby.RubyArray;

public abstract class StepMother {
    protected final List<Class<?>> classes = new ArrayList<Class<?>>();
    protected final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private List<Hook> beforeHooks = new ArrayList<Hook>();

    public abstract void newWorld();

    public void registerClass(Class<?> stepsClass) {
        classes.add(stepsClass);
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    public void executeBeforeHooks(RubyArray arrayWithScenario) throws Throwable {
        for(Hook hook : beforeHooks) {
            hook.invokeOnTarget(arrayWithScenario);
        }
    }

    protected void addCucumberMethods(Object object) {
        for (Method method : object.getClass().getMethods()) {
            registerStepDefinition(object, method);
            registerBefore(object, method);
        }
    }

    private void registerBefore(Object object, Method method) {
        if (method.isAnnotationPresent(Before.class)) {
            beforeHooks.add(new Hook(object, method, method.getAnnotation(Before.class).value()));
        }
    }

    private void registerStepDefinition(Object object, Method method) {
        String regexpString = null;
        if (method.isAnnotationPresent(Given.class)) {
            regexpString = method.getAnnotation(Given.class).value();
        } else if (method.isAnnotationPresent(When.class)) {
            regexpString = method.getAnnotation(When.class).value();
        } else if (method.isAnnotationPresent(Then.class)) {
            regexpString = method.getAnnotation(Then.class).value();
        }
        if (regexpString != null) {
            stepDefinitions.add(new StepDefinition(object, method, regexpString));
        }
    }
}
