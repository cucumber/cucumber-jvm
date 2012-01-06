package cucumber.runtime.groovy;

import cucumber.io.Resource;
import cucumber.io.ResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.World;
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
    private final ResourceLoader resourceLoader;
    private final GroovyShell shell;
    private Closure worldClosure;
    private Object groovyWorld;
    private World world;

    public GroovyBackend(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        instance = this;
        shell = new GroovyShell();
    }

    @Override
    public void buildWorld(List<String> gluePaths, World world) {
        this.world = world;
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
        return new GroovySnippetGenerator(step).getSnippet();
    }

    public void addStepDefinition(Pattern regexp, Closure body) {
        world.addStepDefinition(new GroovyStepDefinition(regexp, body, stepDefLocation(), instance));
    }

    public void registerWorld(Closure closure) {
        worldClosure = closure;
    }

    void addBeforeHook(TagExpression tagExpression, Closure body) {
        world.addBeforeHook(new GroovyHookDefinition(body, tagExpression, instance));
    }

    public void addAfterHook(TagExpression tagExpression, Closure body) {
        world.addAfterHook(new GroovyHookDefinition(body, tagExpression, instance));
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
