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

    public JavaMethodBackend(ObjectFactory objectFactory, MethodFinder methodFinder) {
        this.objectFactory = objectFactory;
        this.methodFinder = methodFinder;
    }

    public List<StepDefinition> getStepDefinitions() {
        Set<Method> methods = methodFinder.getStepDefinitionMethods();
        List<StepDefinition> result = new ArrayList<StepDefinition>();
        for (Method method : methods) {
            objectFactory.addClass(method.getDeclaringClass());
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
