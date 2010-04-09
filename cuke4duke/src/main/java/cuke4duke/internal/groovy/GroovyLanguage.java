package cuke4duke.internal.groovy;

import cuke4duke.GroovyDsl;
import cuke4duke.internal.language.AbstractProgrammingLanguage;
import cuke4duke.internal.language.LanguageMixin;
import cuke4duke.spi.ExceptionFactory;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GroovyLanguage extends AbstractProgrammingLanguage {
    private final List<String> groovyFiles = new ArrayList<String>();
    private Object currentWorld;
    private Closure worldFactory;

    public GroovyLanguage(LanguageMixin languageMixin, ExceptionFactory exceptionFactory) {
        super(languageMixin, exceptionFactory);
        GroovyDsl.groovyLanguage = this;
        GroovyDsl.languageMixin = languageMixin;
    }

    Object invokeClosure(Closure body, Object[] args, Locale locale) throws Throwable {
        body.setDelegate(currentWorld);
        Class[] classes = body.getParameterTypes();
        Object[] transformedArgs = transform(args, classes, locale);
        return body.call(transformedArgs);
    }

    public void begin_scenario(IRubyObject scenario) throws IOException {
        clearHooksAndStepDefinitions();
        worldFactory = null;
        GroovyShell shell = new GroovyShell(new Binding());
        for(String groovyFile : groovyFiles) {
            shell.evaluate(new File(groovyFile));
        }
        currentWorld = worldFactory == null ? new Object() : worldFactory.call();
    }

    public void end_scenario() {
    }

    @Override
    protected Object customTransform(Object arg, Class<?> parameterType, Locale locale) {
        return null;
    }

    public void load_code_file(String groovyFile) throws ClassNotFoundException, IOException {
        groovyFiles.add(groovyFile);
    }

    public void registerWorldFactory(Closure worldFactory) {
        if(this.worldFactory != null) {
            throw new RuntimeException("You can only define one World closure");
        }
        this.worldFactory = worldFactory;
    }
}
