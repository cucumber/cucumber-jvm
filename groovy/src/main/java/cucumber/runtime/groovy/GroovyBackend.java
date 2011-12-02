package cucumber.runtime.groovy;

import cucumber.resources.Consumer;
import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.runtime.Backend;
import cucumber.runtime.World;
import gherkin.TagExpression;
import gherkin.formatter.model.Step;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.List;
import java.util.regex.Pattern;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;


public class GroovyBackend implements Backend {
    static GroovyBackend instance;
    private final GroovyShell shell;
    private Closure worldClosure;
    private Object groovyWorld;
    private World world;

    public GroovyBackend() {
        instance = this;
        shell = new GroovyShell();
    }

    @Override
    public void buildWorld(List<String> gluePaths, World world) {
        this.world = world;

        final Binding context = new Binding();

        for (String gluePath : gluePaths) {
            Resources.scan(gluePath.replace('.', '/'), ".groovy", new Consumer() {
                public void consume(Resource resource) {
                    Script script = shell.parse(resource.getString(), resource.getPath());
                    List respondsTo = script.getMetaClass().respondsTo(script, "main");
                    if (DefaultGroovyMethods.asBoolean(respondsTo)) {
                        script.setBinding(context);
                        script.run();
                    }
                }
            });
        }
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
        world.addBeforeHook(new GroovyHookDefinition(body, tagExpression, instance));
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
            if (stackTraceElement.getFileName().endsWith(".groovy")) {
                return stackTraceElement;
            }
        }
        throw new RuntimeException("Couldn't find location for step definition");
    }
}
