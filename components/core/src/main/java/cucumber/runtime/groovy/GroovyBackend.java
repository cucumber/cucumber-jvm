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
    private final List<Script> scripts;
    private List<StepDefinition> stepDefinitions;

    public GroovyBackend(List<Script> scripts) {
        this.scripts = scripts;
        Dsl.backend = this;
    }

    public List<StepDefinition> getStepDefinitions() {
        stepDefinitions = new ArrayList<StepDefinition>();
        GroovyShell shell = new GroovyShell(new Binding());
        for (Script groovyFile : scripts) {
            // TODO: set the metaClass here instead of in the stepdef file
            shell.evaluate(groovyFile.reader, groovyFile.fileName);
        }
        return stepDefinitions;
    }

    public void newScenario() {
    }

    public void addStepDefinition(Pattern regexp, Closure body, StackTraceElement location) {
        stepDefinitions.add(new GroovyStepDefinition(regexp, body, location));
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
