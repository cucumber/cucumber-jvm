package cucumber.runtime.groovy;

import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.TagExpression;
import gherkin.formatter.model.Step;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static cucumber.runtime.io.MultiLoader.packageName;

public class GroovyBackend implements Backend {
    public static GroovyBackend instance;
    private final Set<Class> scripts = new HashSet<Class>();
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(new GroovySnippet());
    private final ResourceLoader resourceLoader;
    private final GroovyShell shell;
    private final ClassFinder classFinder;

    private Closure worldClosure;
    private Object world;
    private Glue glue;

    private static GroovyShell createShell() {
        CompilerConfiguration compilerConfig = new CompilerConfiguration();
        // Probably not needed:
        // compilerConfig.addCompilationCustomizers(new ASTTransformationCustomizer(ThreadInterrupt.class));
        return new GroovyShell(Thread.currentThread().getContextClassLoader(), new Binding(), compilerConfig);
    }

    public GroovyBackend(ResourceLoader resourceLoader) {
        this(createShell(), resourceLoader);
    }

    public GroovyBackend(GroovyShell shell, ResourceLoader resourceLoader) {
        this.shell = shell;
        this.resourceLoader = resourceLoader;
        instance = this;
        classFinder = new ResourceLoaderClassFinder(resourceLoader, shell.getClassLoader());
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        this.glue = glue;
        final Binding context = shell.getContext();

        for (String gluePath : gluePaths) {
            // Load sources
            for (Resource resource : resourceLoader.resources(gluePath, ".groovy")) {
                Script script = parse(resource);
                runIfScript(context, script);
            }
            // Load compiled scripts
            for (Class<? extends Script> glueClass : classFinder.getDescendants(Script.class, packageName(gluePath))) {
                try {
                    Script script = glueClass.getConstructor(Binding.class).newInstance(context);
                    runIfScript(context, script);
                } catch (Exception e) {
                    throw new CucumberException(e);
                }
            }
        }
    }

    private void runIfScript(Binding context, Script script) {
        Class scriptClass = script.getMetaClass().getTheClass();
        if (isScript(script) && !scripts.contains(scriptClass)) {
            script.setBinding(context);
            script.run();
            scripts.add(scriptClass);
        }
    }

    @Override
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
        //Not used yet
    }

    @Override
    public void buildWorld() {
        world = worldClosure == null ? new Object() : worldClosure.call();
    }

    private Script parse(Resource resource) {
        try {
            return shell.parse(new InputStreamReader(resource.getInputStream(), "UTF-8"), resource.getPath());
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    private boolean isScript(Script script) {
        return DefaultGroovyMethods.asBoolean(script.getMetaClass().respondsTo(script, "main"));
    }

    @Override
    public void disposeWorld() {
        this.world = null;
    }

    @Override
    public String getSnippet(Step step, FunctionNameGenerator functionNameGenerator) {
        return snippetGenerator.getSnippet(step, null);
    }

    public void addStepDefinition(Pattern regexp, long timeoutMillis, Closure body) {
        glue.addStepDefinition(new GroovyStepDefinition(regexp, timeoutMillis, body, currentLocation(), this));
    }

    public void registerWorld(Closure closure) {
        if (worldClosure != null) throw new CucumberException("World is already set");
        worldClosure = closure;
    }

    public void addBeforeHook(TagExpression tagExpression, long timeoutMillis, Closure body) {
        glue.addBeforeHook(new GroovyHookDefinition(tagExpression, timeoutMillis, body, currentLocation(), this));
    }

    public void addAfterHook(TagExpression tagExpression, long timeoutMillis, Closure body) {
        glue.addAfterHook(new GroovyHookDefinition(tagExpression, timeoutMillis, body, currentLocation(), this));
    }

    public void invoke(Closure body, Object[] args) throws Throwable {
        body.setDelegate(world);
        try {
            body.call(args);
        } catch (InvokerInvocationException e) {
            throw e.getCause();
        }
    }

    private static StackTraceElement currentLocation() {
        Throwable t = new Throwable();
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (isGroovyFile(stackTraceElement.getFileName())) {
                return stackTraceElement;
            }
        }
        throw new RuntimeException("Couldn't find location for step definition");
    }

    private static boolean isGroovyFile(String fileName) {
        return fileName != null && fileName.endsWith(".groovy");
    }
}
