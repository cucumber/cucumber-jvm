package cuke4duke.ant;

import org.apache.tools.ant.BuildException;

public class GemTask extends JRubyTask {
    private String args;

    public void execute() throws BuildException {
        createArg().setValue("-S");
        createArg().setValue("gem");
        super.setArgs(args);
        super.execute();
    }

    public void setArgs(String args) {
        this.args = args;
    }
}
