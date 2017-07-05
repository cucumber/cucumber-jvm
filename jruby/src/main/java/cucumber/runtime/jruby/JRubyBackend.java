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
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleStep;
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
    private static final Env ENV = Env.INSTANCE;
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
        String gemPath = ENV.get("GEM_PATH");
        if (gemPath != null) {
            jruby.runScriptlet("ENV['GEM_PATH']='" + gemPath + "'");
        }

        for (Resource resource : resourceLoader.resources("classpath:cucumber/runtime/jruby", ".rb")) {
            runScript(resource);
        }

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
            Iterable<Resource> resources = resourceLoader.resources(gluePath, ".rb");
            List<Resource> resourcesWithEnvFirst = new ArrayList<Resource>();
            for (Resource resource : resources) {
                if (resource.getAbsolutePath().endsWith("env.rb")) {
                    resourcesWithEnvFirst.add(0, resource);
                } else {
                    resourcesWithEnvFirst.add(resource);
                }
            }
            for (Resource resource : resourcesWithEnvFirst) {
                runScript(resource);
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
        for (JRubyWorldDefinition definition : worldDefinitions) {
            currentWorld = definition.execute(currentWorld);
        }
        currentWorld.extend(new IRubyObject[]{CucumberRuntimeJRubyWorld});
    }

    private void runScript(Resource resource) {
        try {
            jruby.runScriptlet(new InputStreamReader(resource.getInputStream(), "UTF-8"), resource.getAbsolutePath());
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
        return snippetGenerator.getSnippet(step, keyword, functionNameGenerator);
    }

    public void registerStepdef(RubyObject stepdefRunner) {
        glue.addStepDefinition(new JRubyStepDefinition(this, stepdefRunner));
    }

    public void registerBeforeHook(RubyObject procRunner, String[] tagExpressions) {
        glue.addBeforeHook(new JRubyHookDefinition(this, tagExpressions, procRunner));
    }

    public void registerAfterHook(RubyObject procRunner, String[] tagExpressions) {
        glue.addAfterHook(new JRubyHookDefinition(this, tagExpressions, procRunner));
    }

    public void registerWorldBlock(RubyObject procRunner) {
        worldDefinitions.add(new JRubyWorldDefinition(procRunner));
    }

    public void pending(String reason) throws PendingException {
        throw new PendingException(reason);
    }

    public void runStep(String featurePath, String language, String stepName, int line, DataTable dataTable, PickleString docString) throws Throwable {
        List<PickleRow> dataTableRows = null;
        if (dataTable != null) {
            dataTableRows = dataTable.getPickleRows();
        }

        unreportedStepExecutor.runUnreportedStep(featurePath, language, stepName, line, dataTableRows, docString);
    }

    public void executeHook(RubyObject hookRunner, Scenario scenario) {
        IRubyObject[] jrubyArgs = new IRubyObject[2];
        jrubyArgs[0] = currentWorld;
        jrubyArgs[1] = JavaEmbedUtils.javaToRuby(hookRunner.getRuntime(), scenario);
        hookRunner.callMethod("execute", jrubyArgs);
    }

    void executeStepdef(RubyObject stepdef, String language, Object[] args) {
        ArrayList<IRubyObject> jrubyArgs = new ArrayList<IRubyObject>();

        // jrubyWorld.@__gherkin_language = language
        RubyObject jrubyI18n = (RubyObject) JavaEmbedUtils.javaToRuby(stepdef.getRuntime(), language);
        currentWorld.callMethod("instance_variable_set", new IRubyObject[]{stepdef.getRuntime().newSymbol("@__gherkin_language"), jrubyI18n});

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
