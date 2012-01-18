package cucumber.runtime.clojure;

import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.RT;
import cucumber.io.Resource;
import cucumber.io.ResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.World;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Pattern;

public class ClojureBackend implements Backend {
    private static ClojureBackend instance;
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(new ClojureSnippet());
    private final ResourceLoader resourceLoader;
    private World world;

    public ClojureBackend(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        instance = this;
        loadScript("cucumber/runtime/clojure/dsl");
    }

    @Override
    public void loadGlue(World world, List<String> gluePaths) {
        this.world = world;
        for (String gluePath : gluePaths) {
            for (Resource resource : resourceLoader.resources(gluePath, ".clj")) {
                loadScript(resource);
            }
        }

    }

    @Override
    public void buildWorld() {
    }

    private void loadScript(String path) {
        try {
            RT.load(path.replaceAll(".clj$", ""), true);
        } catch (IOException e) {
            throw new CucumberException(e);
        } catch (ClassNotFoundException e) {
            throw new CucumberException(e);
        }
    }

    private void loadScript(Resource resource) {
        try {
            Compiler.load(new InputStreamReader(resource.getInputStream(), "UTF-8"), resource.getPath(), resource.getPath());
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(Step step) {
        return snippetGenerator.getSnippet(step);
    }

    private StackTraceElement stepDefLocation(String interpreterClassName, String interpreterMethodName) {
        Throwable t = new Throwable();
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        for (int i = 0; i < stackTraceElements.length; i++) {
            StackTraceElement element = stackTraceElements[i];
            if (element.getClassName().equals(interpreterClassName) && element.getMethodName().equals(interpreterMethodName)) {
                return stackTraceElements[i - 1];
            }
        }
        throw new CucumberException("Couldn't find location for step definition");
    }

    public static void addStepDefinition(Pattern regexp, IFn body) {
        StackTraceElement location = instance.stepDefLocation("clojure.lang.Compiler", "eval");
        instance.world.addStepDefinition(new ClojureStepDefinition(regexp, body, location));
    }

    public static void addBeforeHook(IFn body) {
        instance.world.addBeforeHook(new ClojureHookDefinition(new String[0], body));
    }

    public static void addAfterHook(IFn body) {
        instance.world.addAfterHook(new ClojureHookDefinition(new String[0], body));
    }
}
