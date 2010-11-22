package cucumber.runtime.groovy;

import cucumber.StepDefinition;
import cucumber.runtime.Backend;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GroovyBackend implements Backend {
    private static List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private static Closure worldClosure;
    private static Object world;
    private static GroovyBackend instance;

    public GroovyBackend(List<Script> scripts) {
        instance = this;
        defineStepDefinitions(scripts);
    }

    private void defineStepDefinitions(List<Script> scripts) {
        GroovyShell shell = new GroovyShell(new Binding());
        for (Script groovyFile : scripts) {
            // TODO: set the metaClass here instead of in the stepdef file
            shell.evaluate(groovyFile.reader, groovyFile.fileName);
        }
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    public void newScenario() {
        world = null;
    }

    public static void addStepDefinition(Pattern regexp, Closure body) {
        stepDefinitions.add(new GroovyStepDefinition(regexp, body, stepDefLocation(), instance));
    }

    public static void registerWorld(Closure closure) {
        worldClosure = closure;
    }

    public void invokeStepDefinition(Closure body, Object[] args) {
        body.setDelegate(getWorld());
        body.call(args);
    }

    private Object getWorld() {
        if(world == null) {
            world = worldClosure == null ? new Object() : worldClosure.call();
        }
        return world;
    }

    public static class Script {
        public final Reader reader;
        public final String fileName;

        public Script(Reader reader, String fileName) {
            this.reader = reader;
            this.fileName = fileName;
        }
    }

    private static StackTraceElement stepDefLocation() {
        Throwable t = new Throwable();
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if(stackTraceElement.getFileName().endsWith(".groovy")) {
                return stackTraceElement;
            }
        }
        throw new RuntimeException("Couldn't find location for step definition");
    }
}
