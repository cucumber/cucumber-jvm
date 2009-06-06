package cucumber.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.taskdefs.Java;

import java.util.Arrays;

/**
 * @goal jruby
 */
public class JRubyMojo extends AbstractJRubyMojo {
    /**
     * @parameter expression="${jruby.args}"
     */
    protected String args = null;

    public void execute() throws MojoExecutionException {
        executeCmd(args);
    }

    protected void executeCmd(String commandline) throws MojoExecutionException {
        Java jruby = jruby(Arrays.asList(commandline.split("\\s+")));
        jruby.execute();
    }

}