package cucumber.runtime.groovy;

import cucumber.io.ClasspathResourceLoader;
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
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static cucumber.io.MultiLoader.packageName;


public class GroovyBackend implements Backend {
    static GroovyBackend instance;
    private final Set<Class> scripts = new HashSet<Class>();
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(new GroovySnippet());
    private final ResourceLoader resourceLoader;
    private final GroovyShell shell;
    private final ClasspathResourceLoader classpathResourceLoader;

    private Closure worldClosure;
    private Object groovyWorld;
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
        this.resourceLoader = resourceLoader;
        this.shell = shell;
        instance = this;
        classpathResourceLoader = new ClasspathResourceLoader(shell.getClassLoader());
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
            for (Class<? extends Script> glueClass : classpathResourceLoader.getDescendants(Script.class, packageName(gluePath))) {
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
        this.groovyWorld = null;
    }

    @Override
    public String getSnippet(Step step) {
        return snippetGenerator.getSnippet(step);
    }

    public void addStepDefinition(Pattern regexp, int timeoutMillis, Closure body) {
        glue.addStepDefinition(new GroovyStepDefinition(regexp, timeoutMillis, body, currentLocation(), instance));
    }

    public void registerWorld(Closure closure) {
        worldClosure = closure;
    }

    public void addBeforeHook(TagExpression tagExpression, int timeoutMillis, Closure body) {
        glue.addBeforeHook(new GroovyHookDefinition(tagExpression, timeoutMillis, body, currentLocation(), instance));
    }

    public void addAfterHook(TagExpression tagExpression, int timeoutMillis, Closure body) {
        glue.addAfterHook(new GroovyHookDefinition(tagExpression, timeoutMillis, body, currentLocation(), instance));
    }

    public void invoke(Closure body, Object[] args) throws Throwable {
        body.setDelegate(getGroovyWorld());
        try {
            body.call(args);
        } catch(InvokerInvocationException e) {
            throw e.getCause();
        }
    }

    private Object getGroovyWorld() {
        if (groovyWorld == null) {
            groovyWorld = worldClosure == null ? new Object() : worldClosure.call();
        }
        return groovyWorld;
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
