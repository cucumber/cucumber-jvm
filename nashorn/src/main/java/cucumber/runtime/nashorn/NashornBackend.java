package cucumber.runtime.nashorn;

import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.concurrent.Callable;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

public class NashornBackend implements Backend {
    private static final String JS_DSL = "/cucumber/runtime/nashorn/dsl.js";
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(new JavaScriptSnippet());
    private final ResourceLoader resourceLoader;
    private List<String> gluePaths;
    private Glue glue;
    private Callable buildWorldFn;
    private Callable disposeWorldFn;
    
	private ScriptEngine engine;
	private Bindings engineScope;

    public NashornBackend(ResourceLoader resourceLoader) throws IOException {
        this.resourceLoader = resourceLoader;

        ScriptEngineManager manager = new ScriptEngineManager();
        this.engine = manager.getEngineByName("nashorn");
        

        // define a different script context
        ScriptContext newContext = new SimpleScriptContext();
        newContext.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
        this.engineScope = newContext.getBindings(ScriptContext.ENGINE_SCOPE);
        
        engineScope.put("jsBackend", this);

        Iterable<Resource> resources = resourceLoader.resources("classpath:cucumber/runtime/nashorn", ".js");
		for (Resource resource : resources) {
            runScript(resource);
        }
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        this.glue = glue;
        this.gluePaths = gluePaths;
        for (String gluePath : gluePaths) {
            for (Resource resource : resourceLoader.resources(gluePath, ".js")) {
                try {
					runScript(resource);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
            }
        }
    }

    private void runScript(Resource resource) throws UnsupportedEncodingException, IOException {
        try {
            engine.eval(new InputStreamReader(resource.getInputStream(), "UTF-8"), this.engineScope);
        } catch (ScriptException e) {
            throw new CucumberException("Failed to evaluate JavaScript in " + resource.getAbsolutePath(), e);
        }
    }

    @Override
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
        // Not used yet
    }

    @Override
    public void buildWorld() {
        if (buildWorldFn != null)
			try {
				buildWorldFn.call();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
    }

    @Override
    public void disposeWorld() {
        try {
            if (disposeWorldFn != null) disposeWorldFn.call();
        } catch (Exception e) {
        	throw new RuntimeException(e);
		} finally {
            buildWorldFn = null;
            disposeWorldFn = null;
        }
    }

    public void registerWorld(Callable buildWorldFn, Callable disposeWorldFn) {
        if (this.buildWorldFn != null) throw new CucumberException("World is already set");
        if (buildWorldFn == null) throw new CucumberException("World requires at least a build function");

        this.buildWorldFn = buildWorldFn;
        this.disposeWorldFn = disposeWorldFn;
    }

    @Override
    public String getSnippet(Step step, FunctionNameGenerator functionNameGenerator) {
        return snippetGenerator.getSnippet(step, functionNameGenerator);
    }

    private StackTraceElement jsLocation() {
        Throwable t = new Throwable();
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (stackTraceElement.getClassName().startsWith("jdk.nashorn.internal.scripts.Script")) {
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

    public void addStepDefinition(Object jsStepDefinition, Object regexp, Object bodyFunc, Object argumentsFromFunc) throws Throwable {
        StackTraceElement stepDefLocation = jsLocation();
        NashornStepDefinition stepDefinition = new NashornStepDefinition(this.engine, this.engineScope, jsStepDefinition, regexp, bodyFunc, stepDefLocation, argumentsFromFunc);
        glue.addStepDefinition(stepDefinition);
    }
    
    public void addBeforeHook(Object fn, String[] tags, int order, long timeoutMillis) {
        StackTraceElement stepDefLocation = jsLocation();
        NashornHookDefinition hookDefinition = new NashornHookDefinition(this.engine, this.engineScope, fn, tags, order, timeoutMillis, stepDefLocation);
        glue.addBeforeHook(hookDefinition);
    }
    
    public void addAfterHook(Object fn, String[] tags, int order, long timeoutMillis) {
        StackTraceElement stepDefLocation = jsLocation();
        NashornHookDefinition hookDefinition = new NashornHookDefinition(this.engine, this.engineScope, fn, tags, order, timeoutMillis, stepDefLocation);
        glue.addAfterHook(hookDefinition);
    }
}
