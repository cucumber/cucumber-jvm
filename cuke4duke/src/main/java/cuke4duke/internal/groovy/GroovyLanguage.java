package cuke4duke.internal.groovy;

import cuke4duke.GroovyDsl;
import cuke4duke.internal.language.LanguageMixin;
import cuke4duke.internal.language.AbstractProgrammingLanguage;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroovyLanguage extends AbstractProgrammingLanguage {
    private final List<String> groovyFiles = new ArrayList<String>();
    private Object currentWorld;
    private Closure worldFactory;

    public GroovyLanguage(LanguageMixin languageMixin) {
        super(languageMixin);
        GroovyDsl.groovyLanguage = this;
        GroovyDsl.languageMixin = languageMixin;
    }

    void invokeClosure(Closure body, Object[] args) {
        body.setDelegate(currentWorld);
        body.call(args);
    }

    public void prepareScenario() throws IOException {
        clearHooksAndStepDefinitions();
        worldFactory = null;
        GroovyShell shell = new GroovyShell(new Binding());
        for(String groovyFile : groovyFiles) {
            shell.evaluate(new File(groovyFile));
        }
        currentWorld = worldFactory == null ? new Object() : worldFactory.call();
    }

    public void cleanupScenario() {
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
