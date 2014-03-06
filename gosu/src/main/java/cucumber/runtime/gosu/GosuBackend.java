package cucumber.runtime.gosu;

import cucumber.runtime.Backend;
import cucumber.runtime.Glue;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.io.FileResource;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.formatter.model.Step;
import gw.lang.Gosu;
import gw.lang.function.AbstractBlock;
import gw.lang.reflect.ReflectUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// http://ddebowczyk.github.io/programming/gosu/2013/11/04/gosu-java-interop.html
public class GosuBackend implements Backend {
    private final ResourceLoader resourceLoader;

    public GosuBackend(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        List<File> gosuPath = new ArrayList<File>();
        for (String gluePath : gluePaths) {
            for (Resource resource : resourceLoader.resources(gluePath, ".gs")) {
                if (resource instanceof FileResource) {
                    FileResource fr = (FileResource) resource;
                    gosuPath.add(fr.getFile());
                }
            }
        }
        Gosu.init(gosuPath);

        // TODO: figure out how to avoid using classes.
        // If we have to use classes - how do we figure out what the class name is? Look
        // at the file name?
        Stepdefs stepdefs = ReflectUtil.construct("cucumber.runtime.gosu.test.MyStepdefs");
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

    public void Given(String patternString, Object body) {
        Pattern pattern = Pattern.compile(patternString);
        AbstractBlock block = (AbstractBlock) body;

        // TODO: Store pattern and block in a GosuStepDefinition.
        // For now while we're kicking the tyres of Gosu, just invoke it immediately

        System.out.println(pattern);
        block.invokeWithArgs("YO");
    }
}
