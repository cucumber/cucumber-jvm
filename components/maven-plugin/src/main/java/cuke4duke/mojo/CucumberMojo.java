package cuke4duke.mojo;

import cuke4duke.ant.CucumberTask;
import cuke4duke.internal.Utils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.types.Commandline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @goal cucumber
 */
public class CucumberMojo extends AbstractJRubyMojo {

    /**
     * @parameter expression="${cucumber.features}"
     */
    protected String features = "features";

    /**
     * @parameter expression="${cucumber.installGems}"
     */
    protected boolean installGems = false;

    /**
     * Set this to 'true' to skip running features.
     * 
     * @parameter expression="${maven.test.skip}"
     */
    private boolean skip;

    /**
     * Will cause the project build to look successful, rather than fail, even if there are Cucumber test failures.
     * This can be useful on a continuous integration server, if your only option to be able to collect output files,
     * is if the project builds successfully.
     *
     * @parameter expression="${cucumber.failOnError}"
     */
    protected boolean failOnError = true;

    /**
     * @parameter
     */
    protected List<String> gems;

    /**
     * @parameter
     */
    protected List<String> cucumberArgs = Collections.<String>emptyList();

    /**
     * Appends additional arguments on the command line. e.g.
     * <code>-Dcucumber.extraArgs="--format profile --out target/profile.txt"</code>
     * These arguments will be appended to the cucumberArgs you declare
     * in your POM.
     *
     * @parameter expression="${cucumber.extraArgs}
     */
    protected String extraCucumberArgs;

    /**
     * Extra JVM arguments to pass when running JRuby.
     *
     * @parameter
     */
    protected List<String> jvmArgs;

    public void execute() throws MojoExecutionException {
        if (installGems) {
            for (String gemSpec : gems) {
                installGem(gemSpec);
            }
        }

        if (skip) {
            getLog().info("Cucumber Features are skipped");
            return;
        }

        CucumberTask cucumber = cucumber(allCucumberArgs());
        try {
            cucumber.execute();
        } catch (Exception e) {
            if (failOnError) {
                throw new MojoExecutionException("JRuby failed.", e);
            }
        }
    }

    public CucumberTask cucumber(String args) throws MojoExecutionException {
        CucumberTask cucumber = new CucumberTask();
        cucumber.setProject(getProject());
        for (String jvmArg : getJvmArgs()) {
            if (jvmArg != null) {
                Commandline.Argument arg = cucumber.createJvmarg();
                arg.setValue(jvmArg);
            }
        }
        cucumber.setArgs(args);
        return cucumber;
    }

    String allCucumberArgs() {
        List<String> allCucumberArgs = new ArrayList<String>();
        if (cucumberArgs != null)
            allCucumberArgs.addAll(cucumberArgs);
        if (extraCucumberArgs != null)
            allCucumberArgs.add(extraCucumberArgs);
        allCucumberArgs.add(features);
        return Utils.join(allCucumberArgs.toArray(), " ");
    }

    protected List<String> getJvmArgs() {
        return (jvmArgs != null) ? jvmArgs : Collections.<String>emptyList();
    }
}
