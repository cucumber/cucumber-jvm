package cucumber.runtime.jruby;

import cucumber.io.Resource;
import cucumber.io.ResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.PendingException;
import cucumber.runtime.World;
import cucumber.runtime.snippets.SnippetGenerator;
import cucumber.table.DataTable;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DocString;
import gherkin.formatter.model.Step;
import org.jruby.CompatVersion;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class JRubyBackend implements Backend {
    private static final String DSL = "/cucumber/runtime/jruby/dsl.rb";
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(new JRubySnippet());
    private final ScriptingContainer jruby = new ScriptingContainer();
    private final Set<String> gluedPaths = new HashSet<String>();
    private World world;
    private ResourceLoader resourceLoader;

    //Cache the items, because world is created new each scenario, but the jruby isn't
    //and reloading the entire jruby each time is slow and ineffecient
    private Set<String> loadedResources = new HashSet<String>();
    private Set<RubyObject> stepDefs = new HashSet<RubyObject>();
    private Set<RubyObject> beforeHooks = new HashSet<RubyObject>();
    private Set<RubyObject> afterHooks = new HashSet<RubyObject>();
    private Set<RubyObject> worldBlocks = new HashSet<RubyObject>();


    public JRubyBackend(ResourceLoader resourceLoader) throws UnsupportedEncodingException {
        this.resourceLoader = resourceLoader;
        jruby.put("$backend", this);
        jruby.setClassLoader(getClass().getClassLoader());
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("cucumber-jruby");

            String gemPath = bundle.getString("GEM_PATH");
            if (gemPath != null && !gemPath.isEmpty()) {
                jruby.runScriptlet("ENV['GEM_PATH']='" + gemPath + "'");
            }

            String rubyVersion = bundle.getString("RUBY_VERSION");
            if ("1.9".equals(rubyVersion)) {
                jruby.setCompatVersion(CompatVersion.RUBY1_9);
            }
        } catch (MissingResourceException mre) {
            //Don't actually care
        }
        jruby.runScriptlet(new InputStreamReader(getClass().getResourceAsStream(DSL), "UTF-8"), DSL);
    }

    @Override
    public void buildWorld(List<String> gluePaths, World world) {
        this.world = world;
        jruby.put("$world", new Object());

        //Load all the world preparatory stuff
        for(RubyObject block : worldBlocks) {
            new JRubyWorldBlock(block).execute();
        }
        
        //Inject all the existing step definitions
        for(RubyObject stepdef : stepDefs) {
            world.addStepDefinition(new JRubyStepDefinition(stepdef));
        }
        for(RubyObject beforeHook : beforeHooks) {
            world.addBeforeHook(new JRubyHookDefinition(new String[0], beforeHook));
        }
        for(RubyObject afterHook : afterHooks) {
            world.addAfterHook(new JRubyHookDefinition(new String[0], afterHook));
        }

        for (String gluePath : gluePaths) {
            if (gluedPaths.add(gluePath)) {
                for (Resource resource : resourceLoader.resources(gluePath, ".rb")) {
                if (loadedResources.add(resource.getPath())) {
                    runScriptlet(resource);
                }
                }
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
        return snippetGenerator.getSnippet(step);
    }

    public void pending(String reason) throws PendingException {
        throw new PendingException(reason);
    }

    public void runStep(String uri, Locale locale, String stepKeyword, String stepName, int line, DataTable dataTable, DocString docString) throws Throwable {
        //TODO: convert the data table into a list of dataTableRows for this call
        List<DataTableRow> dataTableRows = null;
        if(dataTable != null) {
            dataTableRows = dataTable.getGherkinRows();
        }

        world.runUnreportedStep(uri, locale, stepKeyword, stepName, line, dataTableRows, docString);
    }

    public void addStepdef(RubyObject stepdef) {
        if (stepDefs.add(stepdef)) {
            world.addStepDefinition(new JRubyStepDefinition(stepdef));
        }
    }

    public void addBeforeHook(RubyObject body) {
        if (beforeHooks.add(body)) {
            world.addBeforeHook(new JRubyHookDefinition(new String[0], body));
        }
    }

    public void addAfterHook(RubyObject body) {
        if (afterHooks.add(body)) {
            world.addAfterHook(new JRubyHookDefinition(new String[0], body));
        }
    }

    public void addWorldBlock(RubyObject body) {
        if(worldBlocks.add(body)) {
            new JRubyWorldBlock(body).execute();
        }
    }
    
}
