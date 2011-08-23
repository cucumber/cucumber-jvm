package cucumber.runtime.jruby;

import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.resources.Consumer;
import cucumber.runtime.Backend;
import cucumber.runtime.StepDefinition;
import gherkin.formatter.model.Step;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class JRubyBackend implements Backend {
    private static final String DSL = "/cucumber/runtime/jruby/dsl.rb";
    private final ScriptingContainer jruby = new ScriptingContainer();
    private final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();

    public JRubyBackend(String packagePrefix) throws UnsupportedEncodingException {
        defineStepDefinitions(packagePrefix.replace('.', '/'));
    }

    private void defineStepDefinitions(String scriptPath) throws UnsupportedEncodingException {
        jruby.put("$backend", this);
        jruby.runScriptlet(new InputStreamReader(getClass().getResourceAsStream(DSL), "UTF-8"), DSL);
        Resources.scan(scriptPath, ".rb", new Consumer() {
            public void consume(Resource resource) {
                jruby.runScriptlet(resource.getReader(), resource.getPath());
            }
        });
    }

    public void registerStepdef(RubyObject stepdef) {
        stepDefinitions.add(new JRubyStepDefinition(stepdef));
    }
    
    @Override
    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    @Override
    public void newWorld() {
        jruby.put("$world", new Object());
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(Step step) {
        return new JRubySnippetGenerator(step).getSnippet();
    }
}
