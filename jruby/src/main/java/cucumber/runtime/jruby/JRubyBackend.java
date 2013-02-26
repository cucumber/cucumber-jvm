package cucumber.runtime.jruby;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.Scenario;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Env;
import cucumber.runtime.Glue;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.I18n;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DocString;
import gherkin.formatter.model.Step;
import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JRubyBackend implements Backend {
    private static final String DSL = "/cucumber/runtime/jruby/dsl.rb";
    private static final Env env = new Env("cucumber-jruby");
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(new JRubySnippet());
    private final ScriptingContainer jruby = new ScriptingContainer();
    private final ResourceLoader resourceLoader;
    private final Set<JRubyWorldDefinition> worldDefinitions = new HashSet<JRubyWorldDefinition>();
    private final RubyModule CucumberRuntimeJRubyWorld;

    private Glue glue;
    private UnreportedStepExecutor unreportedStepExecutor;
    private RubyObject currentWorld;

    public JRubyBackend(ResourceLoader resourceLoader) throws UnsupportedEncodingException {
        this.resourceLoader = resourceLoader;
        jruby.put("$backend", this);
        jruby.setClassLoader(getClass().getClassLoader());
        String gemPath = env.get("GEM_PATH");
        if (gemPath != null) {
            jruby.runScriptlet("ENV['GEM_PATH']='" + gemPath + "'");
        }

        String rubyVersion = env.get("RUBY_VERSION");
        if (rubyVersion != null) {
            // RVM typically defines env vars like
            // RUBY_VERSION=ruby-1.9.3-p362
            if (rubyVersion.matches(".*1\\.8\\.\\d.*") || rubyVersion.matches(".*1\\.8")) {
                jruby.setCompatVersion(CompatVersion.RUBY1_8);
            } else if (rubyVersion.matches(".*1\\.9\\.\\d.*") || rubyVersion.matches(".*1\\.9.*")) {
                jruby.setCompatVersion(CompatVersion.RUBY1_9);
            } else if (rubyVersion.matches(".*2\\.0\\.\\d.*") || rubyVersion.matches(".*2\\.0.*")) {
                jruby.setCompatVersion(CompatVersion.RUBY2_0);
            } else {
                throw new CucumberException("Invalid RUBY_VERSION: " + rubyVersion);
            }
        }
        jruby.runScriptlet(new InputStreamReader(getClass().getResourceAsStream(DSL), "UTF-8"), DSL);

        // Let's go through some hoops to look up the Cucumber::Runtime::JRuby::World module. Sheesh!
        Ruby runtime = jruby.getProvider().getRuntime();
        RubyModule Cucumber = runtime.getModule("Cucumber");
        RubyModule CucumberRuntime = (RubyModule) Cucumber.const_get(runtime.newString("Runtime"));
        RubyModule CucumberRuntimeJRuby = (RubyModule) CucumberRuntime.const_get(runtime.newString("JRuby"));
        CucumberRuntimeJRubyWorld = (RubyModule) CucumberRuntimeJRuby.const_get(runtime.newString("World"));
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
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
        this.unreportedStepExecutor = executor;
    }

    @Override
    public void buildWorld() {
        currentWorld = (RubyObject) JavaEmbedUtils.javaToRuby(jruby.getProvider().getRuntime(), new World());
        currentWorld.extend(new IRubyObject[]{CucumberRuntimeJRubyWorld});

        for (JRubyWorldDefinition definition : worldDefinitions) {
            currentWorld = definition.execute(currentWorld);
        }
    }

    private void runScriptlet(Resource resource) {
        try {
            jruby.runScriptlet(new InputStreamReader(resource.getInputStream(), "UTF-8"), resource.getPath());
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

    public void registerStepdef(RubyObject stepdefRunner) {
        glue.addStepDefinition(new JRubyStepDefinition(this, stepdefRunner));
    }

    public void registerBeforeHook(RubyObject procRunner) {
        glue.addBeforeHook(new JRubyHookDefinition(this, new String[0], procRunner));
    }

    public void registerAfterHook(RubyObject procRunner) {
        glue.addAfterHook(new JRubyHookDefinition(this, new String[0], procRunner));
    }

    public void registerWorldBlock(RubyObject procRunner) {
        worldDefinitions.add(new JRubyWorldDefinition(procRunner));
    }

    public void pending(String reason) throws PendingException {
        throw new PendingException(reason);
    }

    public void runStep(String uri, I18n i18n, String stepKeyword, String stepName, int line, DataTable dataTable, DocString docString) throws Throwable {
        List<DataTableRow> dataTableRows = null;
        if (dataTable != null) {
            dataTableRows = dataTable.getGherkinRows();
        }

        unreportedStepExecutor.runUnreportedStep(uri, i18n, stepKeyword, stepName, line, dataTableRows, docString);
    }

    public void executeHook(RubyObject hookRunner, Scenario scenario) {
        IRubyObject[] jrubyArgs = new IRubyObject[2];
        jrubyArgs[0] = currentWorld;
        jrubyArgs[1] = JavaEmbedUtils.javaToRuby(hookRunner.getRuntime(), scenario);
        hookRunner.callMethod("execute", jrubyArgs);
    }

    void executeStepdef(RubyObject stepdef, I18n i18n, Object[] args) {
        ArrayList<IRubyObject> jrubyArgs = new ArrayList<IRubyObject>();

        // jrubyWorld.@__gherkin_i18n = i18n
        RubyObject jrubyI18n = (RubyObject) JavaEmbedUtils.javaToRuby(stepdef.getRuntime(), i18n);
        currentWorld.callMethod("instance_variable_set", new IRubyObject[]{stepdef.getRuntime().newSymbol("@__gherkin_i18n"), jrubyI18n});

        jrubyArgs.add(currentWorld);

        for (Object o : args) {
            if (o == null) {
                jrubyArgs.add(null);
            } else if (o instanceof DataTable) {
                //Add a datatable as it stands...
                jrubyArgs.add(JavaEmbedUtils.javaToRuby(stepdef.getRuntime(), o));
            } else {
                jrubyArgs.add(stepdef.getRuntime().newString((String) o));
            }
        }

        stepdef.callMethod("execute", jrubyArgs.toArray(new IRubyObject[jrubyArgs.size()]));
    }
}
