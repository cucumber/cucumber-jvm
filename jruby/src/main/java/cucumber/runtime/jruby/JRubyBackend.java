package cucumber.runtime.jruby;

import cucumber.io.Resource;
import cucumber.io.ResourceLoader;
import cucumber.runtime.*;
import cucumber.runtime.snippets.SnippetGenerator;
import cucumber.table.DataTable;
import gherkin.I18n;
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
    private Glue glue;
    private ResourceLoader resourceLoader;
    private UnreportedStepExecutor unreportedStepExecutor;
    private final Set<JRubyWorldBlock> worldBlocks = new HashSet<JRubyWorldBlock>();

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
        jruby.put("$world", new Object());

        for (JRubyWorldBlock block : worldBlocks) {
            block.execute();
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

    public void runStep(String uri, I18n i18n, String stepKeyword, String stepName, int line, DataTable dataTable, DocString docString) throws Throwable {
        List<DataTableRow> dataTableRows = null;
        if (dataTable != null) {
            dataTableRows = dataTable.getGherkinRows();
        }

        unreportedStepExecutor.runUnreportedStep(uri, i18n, stepKeyword, stepName, line, dataTableRows, docString);
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

    public void addWorldBlock(RubyObject body) {
        worldBlocks.add(new JRubyWorldBlock(body));
    }
}
