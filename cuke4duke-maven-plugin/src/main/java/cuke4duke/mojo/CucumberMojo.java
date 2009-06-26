package cuke4duke.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.File;

/**
 * @goal features
 */
public class CucumberMojo extends AbstractJRubyMojo {

    /**
     * @parameter expression="${cucumber.features}"
     */
    private String features;

    /**
     * @parameter expression="${cucumber.installGems}"
     */
    private boolean installGems = false;

    /**
     * @parameter expression="${cucumber.gems}"
     */
    protected String[] gems;

    /**
     * @parameter expression="${cucumber.args}"
     */
    protected String[] args;

    public void execute() throws MojoFailureException, MojoExecutionException {

        if (installGems) {
            installGem(listify("cucumber"));
            for (String s : gems) {
                installGem(parseGem(s));
            }
        }

        List<String> allArgs = new ArrayList<String>();
        allArgs.add(cucumberBin().getAbsolutePath());
        allArgs.addAll(Arrays.asList(args));
        allArgs.add((features != null) ? features : "features");

        Java jruby = jruby(allArgs);
        try {
            jruby.execute();
        } catch (BuildException e) {
            throw new MojoFailureException("Cucumber failed: "+e.getMessage());
        }
    }

    private File cucumberBin() {
        return new File(binDir(), "cucumber");
    }

}
