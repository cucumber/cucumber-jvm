package cucumber.runtime.groovy;

import cucumber.classpath.Classpath;
import cucumber.classpath.Consumer;
import cucumber.classpath.Resource;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.StepDefinition;
import gherkin.formatter.model.Step;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GroovyBackend implements Backend {
    private static GroovyBackend instance;

    private final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private static Closure worldClosure;
    private static Object world;

    public GroovyBackend(String packagePrefix) {
        instance = this;
        try {
            defineStepDefinitions(packagePrefix.replace('.', '/'));
        } catch (IOException e) {
            throw new CucumberException("Couldn't load stepdefs", e);
        }
    }

    private void defineStepDefinitions(String pathPrefix) throws IOException {
        final GroovyShell shell = new GroovyShell(new Binding());
        Classpath.scan(pathPrefix, ".groovy", new Consumer() {
            public void consume(Resource resource) {
                shell.evaluate(resource.getString(), resource.getPath());
            }
        });
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    public void newWorld() {
        world = null;
    }

    public void disposeWorld() {
    }

    public String getSnippet(Step step) {
        return new GroovySnippetGenerator(step).getSnippet();
    }

    public static void addStepDefinition(Pattern regexp, Closure body) {
        instance.stepDefinitions.add(new GroovyStepDefinition(regexp, body, stepDefLocation(), instance));
    }

    public static void registerWorld(Closure closure) {
        worldClosure = closure;
    }

    public void invokeStepDefinition(Closure body, Object[] args) {
        body.setDelegate(getWorld());
        body.call(args);
    }

    private Object getWorld() {
        if (world == null) {
            world = worldClosure == null ? new Object() : worldClosure.call();
        }
        return world;
    }

    private static StackTraceElement stepDefLocation() {
        Throwable t = new Throwable();
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (stackTraceElement.getFileName().endsWith(".groovy")) {
                return stackTraceElement;
            }
        }
        throw new RuntimeException("Couldn't find location for step definition");
    }
}
