package cucumber.runtime.java;

import cucumber.runtime.Backend;
import cucumber.runtime.Classpath;
import cucumber.runtime.SnippetGenerator;
import cucumber.runtime.StepDefinition;
import gherkin.formatter.model.Step;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class JavaBackend implements Backend {
    private final ObjectFactory objectFactory;
    private List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();

    public JavaBackend(String packagePrefix) {
        this.objectFactory = Classpath.instantiateSubclass(ObjectFactory.class);
        new ClasspathMethodScanner().scan(this, packagePrefix);
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    public void newScenario() {
        objectFactory.createInstances();
    }

    public void disposeScenario() {
        objectFactory.disposeInstances();
    }

    public String getSnippet(Step step) {
        return new JavaSnippetGenerator(step).getSnippet();
    }

    void addStepDefinition(Pattern pattern, Method method, Locale locale) {
        Class<?> clazz = method.getDeclaringClass();
        objectFactory.addClass(clazz);
        stepDefinitions.add(new JavaStepDefinition(pattern, method, objectFactory, locale));
    }
}
