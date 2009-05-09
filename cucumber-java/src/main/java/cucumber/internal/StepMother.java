package cucumber.internal;

import cucumber.Given;
import cucumber.Then;
import cucumber.When;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class StepMother {
    protected final List<Class<?>> classes = new ArrayList<Class<?>>();
    protected final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();

    public abstract void newWorld();

    public void registerClass(Class<?> stepsClass) {
        classes.add(stepsClass);
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    protected void addStepDefinitions(Object object) {
        for (Method method : object.getClass().getMethods()) {
            String regexpString = null;
            if (method.isAnnotationPresent(Given.class)) {
                regexpString = method.getAnnotation(Given.class).value();
            } else if (method.isAnnotationPresent(When.class)) {
                regexpString = method.getAnnotation(When.class).value();
            } else if (method.isAnnotationPresent(Then.class)) {
                regexpString = method.getAnnotation(Then.class).value();
            }
            if (regexpString != null) {
                stepDefinitions.add(new StepDefinition(regexpString, object, method));
            }
        }
    }

}
