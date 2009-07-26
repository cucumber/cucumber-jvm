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

    /**
     * @parameter expression="${cucumber.bin}"
     */
    private File cucumberBin;

    @SuppressWarnings({"unchecked"})
    public void execute() throws MojoFailureException, MojoExecutionException {

        if (installGems) {
            for (String s : gems) {
                installGem(parseGem(s));
            }
        }

        List<String> allArgs = new ArrayList<String>();
        allArgs.add("-r");
        allArgs.add("cuke4duke/cucumber_ext");
        allArgs.add(cucumberBin().getAbsolutePath());
        allArgs.addAll(Arrays.asList(args));
        allArgs.add((features != null) ? features : "features");

        Java jruby = jruby(allArgs);
        try {
            jruby.execute();
        } catch (BuildException e) {
            throw new MojoFailureException("Cucumber failed", e);
        }
    }

    private File cucumberBin() {
        return (cucumberBin != null) ? cucumberBin : gemCucumberBin();
    }

    private File gemCucumberBin() {
        return new File(binDir(), "cucumber");
    }

}
