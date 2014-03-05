package cucumber.runtime.gosu;

import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.io.FileResource;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.formatter.model.Step;
import gw.lang.Gosu;
import gw.lang.function.AbstractBlock;
import gw.lang.function.Function1;
import gw.lang.reflect.ReflectUtil;
import sun.org.mozilla.javascript.internal.Function;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

// http://ddebowczyk.github.io/programming/gosu/2013/11/04/gosu-java-interop.html
public class GosuBackend implements Backend {
    private final ResourceLoader resourceLoader;
    private Glue glue;
    private List<String> gluePaths;

    public GosuBackend(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        System.out.println("gluePaths = " + gluePaths);

        this.glue = glue;
        this.gluePaths = gluePaths;

        List<File> gosuPath = new ArrayList<File>();
        for (String gluePath : gluePaths) {
            for (Resource resource : resourceLoader.resources(gluePath, ".gs")) {
                if(resource instanceof FileResource) {
                    FileResource fr = (FileResource) resource;
                    System.out.println("resource = " + fr.getFile().getAbsolutePath());
                    gosuPath.add(fr.getFile());
                }
            }
        }
        Gosu.init(gosuPath);

        Stepdefs stepdefs = ReflectUtil.construct("cucumber.runtime.gosu.test.MyStepdefs");
        System.out.println(stepdefs);
        stepdefs.define(this);
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

    public void Given(Object pattern, Object body) {
        System.out.println("pattern = " + pattern.getClass() + ":" + pattern);
        AbstractBlock block = (AbstractBlock) body;
        block.invokeWithArgs("YO");
    }
}
