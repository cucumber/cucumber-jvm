package cucumber.runtime.jruby;

import cucumber.io.Resource;
import cucumber.io.ResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.PendingException;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;
import org.jruby.CompatVersion;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class JRubyBackend implements Backend {
    private static final String DSL = "/cucumber/runtime/jruby/dsl.rb";
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(new JRubySnippet());
    private final ScriptingContainer jruby = new ScriptingContainer();
    private Glue glue;
    private ResourceLoader resourceLoader;

    public JRubyBackend(ResourceLoader resourceLoader) throws UnsupportedEncodingException {
        this.resourceLoader = resourceLoader;
        jruby.put("$backend", this);
        jruby.setClassLoader(getClass().getClassLoader());
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
    public void loadGlue(Glue glue, List<String> gluePaths) {
        this.glue = glue;
        for (String gluePath : gluePaths) {
            for (Resource resource : resourceLoader.resources(gluePath, ".rb")) {
                runScriptlet(resource);
            }
        }
    }

    @Override
    public void buildWorld() {
        jruby.put("$world", new Object());
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
        return snippetGenerator.getSnippet(step);
    }

    public void pending(String reason) throws PendingException {
        throw new PendingException(reason);
    }

    public void runStep(String uri, Locale locale, String stepKeyword, String stepName, int line) throws Throwable {
        glue.runUnreportedStep(uri, locale, stepKeyword, stepName, line);
    }

    public void addStepdef(RubyObject stepdef) {
        glue.addStepDefinition(new JRubyStepDefinition(stepdef));
    }

    public void addBeforeHook(RubyObject body) {
        glue.addBeforeHook(new JRubyHookDefinition(new String[0], body));
    }

    public void addAfterHook(RubyObject body) {
        glue.addAfterHook(new JRubyHookDefinition(new String[0], body));
    }

}
