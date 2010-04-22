package cuke4duke.ant;

import org.apache.tools.ant.BuildException;

import java.io.File;

public class GemTask extends JRubyTask {
    private String args = "";

    public GemTask() {
        createJvmarg().setValue("-Xmx384m");
    }

    public void execute() throws BuildException {
        createArg().setValue("-S");
        createArg().setValue("gem");
        getCommandLine().createArgument().setLine(args);
        createArg().setValue("--install-dir");
        createArg().setFile(getJrubyHome());
        createArg().setValue("--no-ri");
        createArg().setValue("--no-rdoc");

        super.execute();
    }

    public void setArgs(String args) {
        this.args = args;
    }
}
