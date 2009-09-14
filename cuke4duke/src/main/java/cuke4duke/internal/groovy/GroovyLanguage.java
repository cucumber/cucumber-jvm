package cuke4duke.internal.groovy;

import cuke4duke.GroovyDsl;
import cuke4duke.internal.StringConverter;
import cuke4duke.internal.language.LanguageMixin;
import cuke4duke.internal.language.ProgrammingLanguage;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import org.jruby.RubyArray;

import java.io.File;
import java.io.IOException;

public class GroovyLanguage extends ProgrammingLanguage {
    private final GroovyShell shell;
    private Object currentWorld;
    private Closure worldFactory;

    public GroovyLanguage(LanguageMixin languageMixin) {
        super(languageMixin);
        GroovyDsl.groovyLanguage = this;
        GroovyDsl.languageMixin = languageMixin;
        Binding binding = new Binding();
        shell = new GroovyShell(binding);
    }

    void invokeClosure(Closure body, RubyArray args) {
        Object[] converted = new StringConverter().convert(body.getParameterTypes(), args);
        body.setDelegate(currentWorld);
        body.call(converted);
    }

    public void begin_scenario() {
        currentWorld = worldFactory == null ? new Object() : worldFactory.call();
    }

    public void end_scenario() {
        currentWorld = null;
    }

    protected void load(String groovy_file) throws ClassNotFoundException, IOException {
        shell.evaluate(new File(groovy_file));
    }

    public void registerWorldFactory(Closure worldFactory) {
        if(this.worldFactory != null) {
            throw new RuntimeException("You can only define one World closure");
        }
        this.worldFactory = worldFactory;
    }
}
