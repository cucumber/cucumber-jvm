package cucumber.runtime.jruby;

import cucumber.resources.Consumer;
import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.runtime.Backend;
import cucumber.runtime.World;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.jruby.CompatVersion;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class JRubyBackend implements Backend {
    private static final String DSL = "/cucumber/runtime/jruby/dsl.rb";
    private final ScriptingContainer jruby = new ScriptingContainer();
    private World world;

    public JRubyBackend() throws UnsupportedEncodingException {
        jruby.put("$backend", this);
        //Look for a cucumber-jruby.properties file and load in the things I might care about
        Properties props = new Properties();
        InputStream propsStream = this.getClass().getResourceAsStream("/cucumber-jruby.properties");
        try {
            if (propsStream != null) {
                props.load(propsStream);
            }
        } catch (IOException e) {
            //Oh well?
        }

        String gemPath = props.getProperty("GEM_PATH");
        if (gemPath != null && !gemPath.isEmpty()) {
            jruby.runScriptlet("ENV['GEM_PATH']='" + gemPath + "'");
        }

        String rubyVersion = props.getProperty("RUBY_VERSION");
        if ("1.9".equals(rubyVersion)) {
            jruby.setCompatVersion(CompatVersion.RUBY1_9);
        }
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

    public void runStep(String uri, Locale locale, String stepString) throws Throwable {
        Step s = new Step(Collections.<Comment>emptyList(), "Given ", stepString, 0, null, null);
        world.runUnreportedStep(uri, s, locale);
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
