package cucumber.runtime.groovy;

import cucumber.resources.Consumer;
import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.runtime.Backend;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.World;
import gherkin.formatter.model.Step;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GroovyBackend implements Backend {
    private static GroovyBackend instance;
    private final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private final GroovyShell shell;
    private Closure worldClosure;
    private Object groovyWorld;
    private World world;

    public GroovyBackend() {
        instance = this;
        shell = new GroovyShell(new Binding());
    }

    @Override
    public void buildWorld(List<String> codePaths, World world) {
        this.world = world;
        for (String codePath : codePaths) {
            Resources.scan(codePath.replace('.', '/'), ".groovy", new Consumer() {
                public void consume(Resource resource) {
                    shell.evaluate(resource.getString(), resource.getPath());
                }
            });
        }
    }

    @Override
    public void disposeWorld() {
        this.groovyWorld = null;
    }

    @Override
    public String getSnippet(Step step) {
        return new GroovySnippetGenerator(step).getSnippet();
    }

    public static void addStepDefinition(Pattern regexp, Closure body) {
        instance.stepDefinitions.add(new GroovyStepDefinition(regexp, body, stepDefLocation(), instance));
    }

    public static void registerWorld(Closure closure) {
        instance.worldClosure = closure;
    }

    public void invokeStepDefinition(Closure body, Object[] args) {
        body.setDelegate(getGroovyWorld());
        body.call(args);
    }

    private Object getGroovyWorld() {
        if (groovyWorld == null) {
            groovyWorld = worldClosure == null ? new Object() : worldClosure.call();
        }
        return groovyWorld;
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
