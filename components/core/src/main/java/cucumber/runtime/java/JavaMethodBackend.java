package cucumber.runtime.java;

import cucumber.StepDefinition;
import cucumber.runtime.Backend;
import cuke4duke.annotation.I18n;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class JavaMethodBackend implements Backend {
    private final ObjectFactory objectFactory;
    private final MethodFinder methodFinder;
    private List<StepDefinition> stepDefinitions;

    public JavaMethodBackend(ObjectFactory objectFactory, MethodFinder methodFinder) {
        this.objectFactory = objectFactory;
        this.methodFinder = methodFinder;
        stepDefinitions = createStepDefinitions();
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    private List<StepDefinition> createStepDefinitions() {
        Set<Method> methods = methodFinder.getStepDefinitionMethods();
        List<StepDefinition> result = new ArrayList<StepDefinition>();
        for (Method method : methods) {
            Class<?> type = method.getDeclaringClass();
            objectFactory.addClass(type);
            // TODO: Look for other annotations too.
            Pattern pattern = Pattern.compile(method.getAnnotation(I18n.EN.Given.class).value());
            result.add(new JavaMethodStepDefinition(pattern, method, objectFactory));
        }

        return result;
    }

    public void newScenario() {
        objectFactory.createObjects();
    }
}
