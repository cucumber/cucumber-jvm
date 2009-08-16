package cuke4duke.internal.groovy;

import cuke4duke.internal.StringConverter;
import cuke4duke.internal.language.ProgrammingLanguage;
import cuke4duke.internal.language.StepMother;
import cuke4duke.GroovyDsl;
import org.jruby.RubyArray;
import groovy.lang.Closure;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.IOException;

public class GroovyLanguage extends ProgrammingLanguage {
    private final GroovyShell shell;
    private Object currentWorld;

    public GroovyLanguage(StepMother stepMother) {
        super(stepMother);
        GroovyDsl.stepMother = stepMother;
        GroovyDsl.groovyLanguage = this;
        Binding binding = new Binding();
        shell = new GroovyShell(binding);
    }

    void invokeClosure(Closure body, RubyArray args) {
        Object[] converted = new StringConverter().convert(body.getParameterTypes(), args);
        body.setDelegate(currentWorld);
        body.call(converted);
    }

    public void load_step_def_file(String step_def_file) throws IOException {
        shell.evaluate(new File(step_def_file));
      }

    public void begin_scenario() {
        currentWorld = new Object();
    }

    public void end_scenario() {
        currentWorld = null;
    }

}
