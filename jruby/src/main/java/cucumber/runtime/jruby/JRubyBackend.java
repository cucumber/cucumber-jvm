package cucumber.runtime.jruby;

import cucumber.resources.Consumer;
import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.runtime.Backend;
import cucumber.runtime.World;
import gherkin.formatter.model.Step;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;

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
        for (String gluePath : gluePaths) {
            Resources.scan(gluePath.replace('.', '/'), ".rb", new Consumer() {
                public void consume(Resource resource) {
                    jruby.runScriptlet(resource.getReader(), resource.getPath());
                }
            });
        }
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(Step step) {
        return new JRubySnippetGenerator(step).getSnippet();
    }

    public void registerStepdef(RubyObject stepdef) {
        world.addStepDefinition(new JRubyStepDefinition(stepdef));
    }
    
    public void addBeforeHook(RubyObject body) {
    	world.addBeforeHook(new JRubyHookDefinition(new String[0],body));
    }
    
    public void addAfterHook(RubyObject body) {
    	world.addAfterHook(new JRubyHookDefinition(new String[0],body));
    }

    public void addBeforeHook(RubyObject body) {
        world.addBeforeHook(new JRubyHookDefinition(new String[0], body));
    }

    public void addAfterHook(RubyObject body) {
        world.addAfterHook(new JRubyHookDefinition(new String[0], body));
    }

}
