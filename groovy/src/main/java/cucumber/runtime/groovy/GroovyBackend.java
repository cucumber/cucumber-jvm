package cucumber.runtime.groovy;

import cucumber.io.Resource;
import cucumber.io.ResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.TagExpression;
import gherkin.formatter.model.Step;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Pattern;


public class GroovyBackend implements Backend {
    static GroovyBackend instance;
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(new GroovySnippet());
    private final ResourceLoader resourceLoader;
    private final GroovyShell shell;
    private Closure worldClosure;
    private Object groovyWorld;
    private Glue glue;

    public GroovyBackend(ResourceLoader resourceLoader) {
        this (new GroovyShell(), resourceLoader);
    }

    public GroovyBackend(GroovyShell shell, ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.shell = shell;
        instance = this;
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        this.glue = glue;
        final Binding context = new Binding();

        for (String gluePath : gluePaths) {
            for (Resource resource : resourceLoader.resources(gluePath, ".groovy")) {
                Script script = parse(resource);
                if (isScript(script)) {
                    script.setBinding(context);
                    script.run();
                }
            }
        }
    }

    @Override
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
        //Not used yet
    }

    @Override
    public void buildWorld() {
    }

    private Script parse(Resource resource) {
        try {
            return shell.parse(new InputStreamReader(resource.getInputStream()), resource.getPath());
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    private boolean isScript(Script script) {
        return DefaultGroovyMethods.asBoolean(script.getMetaClass().respondsTo(script, "main"));
    }

    @Override
    public void disposeWorld() {
        this.groovyWorld = null;
    }

    @Override
    public String getSnippet(Step step) {
        return snippetGenerator.getSnippet(step);
    }

    public void addStepDefinition(Pattern regexp, Closure body) {
        glue.addStepDefinition(new GroovyStepDefinition(regexp, body, stepDefLocation(), instance));
    }

    public void registerWorld(Closure closure) {
        worldClosure = closure;
    }

    void addBeforeHook(TagExpression tagExpression, Closure body) {
        glue.addBeforeHook(new GroovyHookDefinition(body, tagExpression, instance));
    }

    public void addAfterHook(TagExpression tagExpression, Closure body) {
        glue.addAfterHook(new GroovyHookDefinition(body, tagExpression, instance));
    }

    public void invoke(Closure body, Object[] args) {
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
            if (isGroovyFile (stackTraceElement.getFileName ())) {
                return stackTraceElement;
            }
        }
        throw new RuntimeException("Couldn't find location for step definition");
    }
    
    private static boolean isGroovyFile (String fileName) {
        return fileName != null && fileName.endsWith (".groovy");
    }
}
