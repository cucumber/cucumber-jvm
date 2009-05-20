package cucumber.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

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

    @SuppressWarnings({"unchecked"})
    public void execute() throws MojoFailureException, MojoExecutionException {

        if (installGems) {
            installGem(listify("cucumber"));
            for (String s : gems) {
                installGem(parseGem(s));
            }
        }
        
        List<String> allArgs = new ArrayList<String>();
        allArgs.add("-S");
        allArgs.add("cucumber");
		allArgs.addAll(Arrays.asList(args));
        allArgs.add((features != null) ? features : "src/test/features");

        Java jruby = jruby(allArgs);
        try {
            jruby.execute();
        } catch (BuildException e) {
            // suck it & spit
            throw new MojoFailureException("Cucumber failed: " + e.getMessage());
        }
    }

    private List parseGem(String gemSpec) throws MojoExecutionException {

        List<String> gemArgs = new ArrayList<String>();
        String[] gem = gemSpec.split(":");

        String name = gem.length > 0 ? gem[0] : null;
        String version = gem.length > 1 ? gem[1] : null;
        String source = gem.length > 2 ? gem[2] : null;

        if (name == null || name.isEmpty()) {
            throw new MojoExecutionException("Requires atleast a name for <gem>");
        } else {
            gemArgs.add(name);
        }

        if (version != null && !version.isEmpty()) {
            gemArgs.add("-v" + version);
        }

        if (source != null && !source.isEmpty()) {
            if (source.contains("github")) {
                gemArgs.add("--source");
                gemArgs.add("http://gems.github.com");
            }
        }
        return gemArgs;
    }

}
