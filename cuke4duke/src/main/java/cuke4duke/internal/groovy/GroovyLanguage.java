package cuke4duke.internal.groovy;

import cuke4duke.GroovyDsl;
import cuke4duke.internal.ArgumentsConverter;
import cuke4duke.internal.language.LanguageMixin;
import cuke4duke.internal.language.ProgrammingLanguage;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
        Object[] converted = new ArgumentsConverter().convert(body.getParameterTypes(), args);
        body.setDelegate(currentWorld);
        body.call(converted);
    }

    public void begin_scenario() {
        currentWorld = worldFactory == null ? new Object() : worldFactory.call();
    }

    public void end_scenario() {
        currentWorld = null;
    }

    public void load_code_file(String groovy_file) throws ClassNotFoundException, IOException {
        shell.evaluate(new File(groovy_file));
    }

    @Override
    public List<IRubyObject> step_match_list(String step_name, String formatted_step_name) {
        throw new UnsupportedOperationException("Fixme");
    }

    public void registerWorldFactory(Closure worldFactory) {
        if(this.worldFactory != null) {
            throw new RuntimeException("You can only define one World closure");
        }
        this.worldFactory = worldFactory;
    }
}
