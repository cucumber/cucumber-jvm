package cucumber.runtime.groovy;

import cucumber.resources.Consumer;
import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.HookDefinition;
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
    private List<HookDefinition> beforeHooks = new ArrayList<HookDefinition>();
    private List<HookDefinition> afterHooks = new ArrayList<HookDefinition>();
    
    private static Closure worldClosure;
    private static Object world;

    public GroovyBackend(List<String> scriptPaths) {
        instance = this;
        try {
            defineStepDefinitions(scriptPaths);
        } catch (IOException e) {
            throw new CucumberException("Couldn't load stepdefs", e);
        }
    }

    private void defineStepDefinitions(List<String> scriptPaths) throws IOException {
        final GroovyShell shell = new GroovyShell(new Binding());
        for (String scriptPath : scriptPaths) {
            Resources.scan(scriptPath.replace('.', '/'), ".groovy", new Consumer() {
                public void consume(Resource resource) {
                    shell.evaluate(resource.getString(), resource.getPath());
                }
            });
        }
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
        StackTraceElement potentialMatch = null;
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (potentialMatch != null && GroovyShell.class.getName().equals(stackTraceElement.getClassName())) {
                return potentialMatch;
            } else if (stackTraceElement.getFileName() != null && stackTraceElement.getFileName().endsWith(".groovy")) {
                potentialMatch = stackTraceElement;
            }
        }
        throw new RuntimeException("Couldn't find location for step definition");
    }

    @Override
    public List<HookDefinition> getBeforeHooks() {
        return beforeHooks;
    }

    @Override
    public List<HookDefinition> getAfterHooks() {
        return afterHooks;
    }

    public static void addBeforeHook(HookDefinition hook) {
        instance.beforeHooks.add(hook);
    }

    public static void addAfterHook(HookDefinition hook) {
        instance.afterHooks.add(hook);
    }
}
