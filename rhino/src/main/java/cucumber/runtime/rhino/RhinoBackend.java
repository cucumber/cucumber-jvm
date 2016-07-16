package cucumber.runtime.rhino;

import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.pickles.PickleStep;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.regexp.NativeRegExp;
import org.mozilla.javascript.tools.shell.Global;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class RhinoBackend implements Backend {
    private static final String JS_DSL = "/cucumber/runtime/rhino/dsl.js";
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(new JavaScriptSnippet());
    private final ResourceLoader resourceLoader;
    private final Context cx;
    private final Scriptable scope;
    private List<String> gluePaths;
    private Glue glue;
    private Function buildWorldFn;
    private Function disposeWorldFn;

    public RhinoBackend(ResourceLoader resourceLoader) throws IOException {
        this.resourceLoader = resourceLoader;
        cx = Context.enter();
        scope = new Global(cx); // This gives us access to global functions like load()
        scope.put("jsBackend", scope, this);

        for (Resource resource : resourceLoader.resources("classpath:cucumber/runtime/rhino", ".js")) {
            runScript(resource);
        }
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        this.glue = glue;
        this.gluePaths = gluePaths;
        for (String gluePath : gluePaths) {
            for (Resource resource : resourceLoader.resources(gluePath, ".js")) {
                runScript(resource);
            }
        }
    }

    private void runScript(Resource resource) {
        try {
            cx.evaluateReader(scope, new InputStreamReader(resource.getInputStream(), "UTF-8"), resource.getAbsolutePath(), 1, null);
        } catch (IOException e) {
            throw new CucumberException("Failed to evaluate JavaScript in " + resource.getAbsolutePath(), e);
        }
    }

    @Override
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
        // Not used yet
    }

    @Override
    public void buildWorld() {
        if (buildWorldFn != null) buildWorldFn.call(cx, scope, scope, new Object[0]);
    }

    @Override
    public void disposeWorld() {
        try {
            if (disposeWorldFn != null) disposeWorldFn.call(cx, scope, scope, new Object[0]);
        } finally {
            buildWorldFn = null;
            disposeWorldFn = null;
        }
    }

    public void registerWorld(Function buildWorldFn, Function disposeWorldFn) {
        if (this.buildWorldFn != null) throw new CucumberException("World is already set");
        if (buildWorldFn == null) throw new CucumberException("World requires at least a build function");

        this.buildWorldFn = buildWorldFn;
        this.disposeWorldFn = disposeWorldFn;
    }

    @Override
    public String getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
        return snippetGenerator.getSnippet(step, keyword, functionNameGenerator);
    }

    private StackTraceElement jsLocation() {
        Throwable t = new Throwable();
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            boolean js = stackTraceElement.getFileName().endsWith(".js");
            if (js) {
                boolean isDsl = stackTraceElement.getFileName().endsWith(JS_DSL);
                boolean hasLine = stackTraceElement.getLineNumber() != -1;
                if (!isDsl && hasLine) {
                    return stackTraceElement;
                }
//                System.out.println("stackTraceElement.getFileName() = " + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber());
//                for (String gluePath : gluePaths) {
//                    boolean inScriptPath = packageName(stackTraceElement.getFileName()).startsWith(packageName(gluePath));
//                    boolean hasLine = stackTraceElement.getLineNumber() != -1;
//                    if (inScriptPath && hasLine) {
//                        return stackTraceElement;
//                    }
//                }
            }
        }
        throw new RuntimeException("Couldn't find location for step definition");
    }

    public void addStepDefinition(Global jsStepDefinition, NativeRegExp regexp, NativeFunction bodyFunc, NativeFunction argumentsFromFunc) throws Throwable {
        StackTraceElement stepDefLocation = jsLocation();
        RhinoStepDefinition stepDefinition = new RhinoStepDefinition(cx, scope, jsStepDefinition, regexp, bodyFunc, stepDefLocation, argumentsFromFunc);
        glue.addStepDefinition(stepDefinition);
    }

    public void addBeforeHook(Function fn, String[] tags, int order, long timeoutMillis) {
        StackTraceElement stepDefLocation = jsLocation();
        RhinoHookDefinition hookDefinition = new RhinoHookDefinition(cx, scope, fn, tags, order, timeoutMillis, stepDefLocation);
        glue.addBeforeHook(hookDefinition);
    }

    public void addAfterHook(Function fn, String[] tags, int order, long timeoutMillis) {
        StackTraceElement stepDefLocation = jsLocation();
        RhinoHookDefinition hookDefinition = new RhinoHookDefinition(cx, scope, fn, tags, order, timeoutMillis, stepDefLocation);
        glue.addAfterHook(hookDefinition);
    }
}
