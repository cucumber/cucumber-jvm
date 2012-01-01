package cucumber.runtime.jruby;

import cucumber.io.Resource;
import cucumber.io.ResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
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

    public JRubyBackend() throws UnsupportedEncodingException {
        jruby.put("$backend", this);
        jruby.runScriptlet(new InputStreamReader(getClass().getResourceAsStream(DSL), "UTF-8"), DSL);
    }

    @Override
    public void buildWorld(List<String> gluePaths, World world) {
        this.world = world;
        jruby.put("$world", new Object());
        Iterable<Resource> resources = new ResourceLoader().fileResources(gluePaths, ".rb");
        for (Resource resource : resources) {
            runScriptlet(resource);
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
