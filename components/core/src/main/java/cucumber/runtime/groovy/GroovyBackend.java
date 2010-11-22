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
    private List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();

    private Closure worldClosure;
    private Object world;

    public GroovyBackend(List<Script> scripts) {
        Dsl.backend = this;
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

    public void addStepDefinition(Pattern regexp, Closure body, StackTraceElement location) {
        stepDefinitions.add(new GroovyStepDefinition(regexp, body, location, this));
    }

    public void registerWorld(Closure worldClosure) {
        this.worldClosure = worldClosure;
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
}
