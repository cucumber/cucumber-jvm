package cucumber.runtime.gosu;

import cucumber.runtime.Backend;
import cucumber.runtime.Glue;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.formatter.model.Step;
import gw.lang.Gosu;
import gw.lang.function.AbstractBlock;
import gw.lang.launch.IArgInfo;

import java.util.List;
import java.util.regex.Pattern;

public class GosuBackend implements Backend {
    private final ResourceLoader resourceLoader;

    public GosuBackend(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        GlueSource source = new GlueSource();

        for (String gluePath : gluePaths) {
            for (Resource glueScript : resourceLoader.resources(gluePath, ".gsp")) {
                source.addGlueScript(glueScript);
            }
        }

        Gosu gosu = new Gosu();
        gosu.start(source.toArgInfo());
    }

    @Override
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {

    }

    @Override
    public void buildWorld() {

    }

    @Override
    public void disposeWorld() {

    }

    @Override
    public String getSnippet(Step step, FunctionNameGenerator functionNameGenerator) {
        return null;
    }
}
