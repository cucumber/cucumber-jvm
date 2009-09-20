package cuke4duke.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.taskdefs.Java;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @goal features
 */
public class CucumberMojo extends AbstractJRubyMojo {

    /**
     * @parameter expression="${cucumber.features}"
     */
    protected String features;

    /**
     * @parameter expression="${cucumber.installGems}"
     */
    protected boolean installGems = false;

    /**
     * @parameter
     */
    protected List<String> gems;

    /**
     * @parameter
     * @deprecated
     */
    protected List<String> args;

    /**
     * @parameter
     */
    protected List<String> cucumberArgs;

    /**
     * Appends additional arguments on the command line. e.g.
     * <code>-Dcucumber.extraArgs="--format profile --out target/profile.txt"</code>
     *
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

    /**
     * @parameter expression="${cucumber.bin}"
     */
    protected File cucumberBin;

    public void execute() throws MojoExecutionException {

        if (installGems) {
            for (String gemSpec : gems) {
                installGem(gemSpec);
            }
        }

        List<String> allArgs = new ArrayList<String>();
        allArgs.add("-r");
        allArgs.add("cuke4duke/cucumber_ext");
        allArgs.add(cucumberBin().getAbsolutePath());
        if (args != null)
            allArgs.addAll(args);
        allArgs.addAll(addCucumberArgs());
        allArgs.add((features != null) ? features : "features");

        Java jruby = jruby(allArgs);
        jruby.execute();
    }

    List<String> addCucumberArgs() {
        List<String> allCucumberArgs = new ArrayList<String>();
        if (cucumberArgs != null)
            allCucumberArgs.addAll(cucumberArgs);
        if (extraCucumberArgs != null)
            allCucumberArgs.addAll(Arrays.asList(extraCucumberArgs.split(" ")));
        return allCucumberArgs;
    }

    private File cucumberBin() {
        return (cucumberBin != null) ? cucumberBin : gemCucumberBin();
    }

    private File gemCucumberBin() {
        return new File(binDir(), "cucumber");
    }

    protected List<String> getJvmArgs() {
        return (jvmArgs != null) ? jvmArgs : new ArrayList<String>();
    }
}
