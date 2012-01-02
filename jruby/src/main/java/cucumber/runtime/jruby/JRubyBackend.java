package cucumber.runtime.jruby;

import cucumber.io.Resource;
import cucumber.io.ResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.PendingException;
import cucumber.runtime.World;
import gherkin.formatter.model.Step;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class JRubyBackend implements Backend {
    private static final String DSL = "/cucumber/runtime/jruby/dsl.rb";
    private final ScriptingContainer jruby = new ScriptingContainer();
    private World world;
    private ResourceLoader resourceLoader;

    public JRubyBackend(ResourceLoader resourceLoader) throws UnsupportedEncodingException {
        this.resourceLoader = resourceLoader;
        jruby.put("$backend", this);
        jruby.setClassLoader(getClass().getClassLoader());
        jruby.runScriptlet(new InputStreamReader(getClass().getResourceAsStream(DSL), "UTF-8"), DSL);
    }

    @Override
    public void buildWorld(List<String> gluePaths, World world) {
        this.world = world;
        jruby.put("$world", new Object());

        for (String gluePath : gluePaths) {
            for (Resource resource : resourceLoader.resources(gluePath, ".rb")) {
                runScriptlet(resource);
            }
        }
    }

    private void runScriptlet(Resource resource) {
        try {
            jruby.runScriptlet(new InputStreamReader(resource.getInputStream()), resource.getPath());
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(Step step) {
        return new JRubySnippetGenerator(step).getSnippet();
    }

    public void pending(String reason) throws PendingException {
        throw new PendingException(reason);
    }

    public void addStepdef(RubyObject stepdef) {
        world.addStepDefinition(new JRubyStepDefinition(stepdef));
    }

    public void addBeforeHook(RubyObject body) {
        world.addBeforeHook(new JRubyHookDefinition(new String[0], body));
    }

    public void addAfterHook(RubyObject body) {
        world.addAfterHook(new JRubyHookDefinition(new String[0], body));
    }

}
