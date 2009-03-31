package cucumber.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @goal features
 */
public class CucumberMojo extends AbstractJRubyMojo {

    /**
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter expression="${cucumber.features.directory}"
     */
    private String featuresDirectory;

    @Override
    public void execute() throws MojoFailureException, MojoExecutionException {
        outputDirectory.mkdirs();
        ensureGem("cucumber");
        List<String> allArgs = new ArrayList<String>();
        allArgs.add("-S");
        allArgs.add("cucumber");
        if (featuresDirectory != null) {
            allArgs.add(featuresDirectory);
        } else {
            allArgs.add("src/test/features");
        }

        Java jruby = null;
        jruby = jruby((String[]) allArgs.toArray(new String[allArgs.size()]));
        try {
            jruby.execute();
        } catch (BuildException e) {
            // suck it & spit
            throw new MojoFailureException("Cucumber failed: " + e.getMessage());
        }
    }
}
