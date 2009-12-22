package cuke4duke.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Environment;

public class CucumberTask extends JRubyTask {
    private String args = "";

    public void execute() throws BuildException {
        createArg().setFile(getCuke4dukeBinFile());
        getCommandLine().createArgument().setLine(args);
        super.execute();
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public void setObjectFactory(String name) {
        Environment.Variable objectFactory = new Environment.Variable();
        objectFactory.setKey("cuke4duke.objectFactory");
        objectFactory.setValue("cuke4duke.internal.jvmclass." + name.substring(0,1).toUpperCase() + name.substring(1) + "Factory");
        this.addSysproperty(objectFactory);
    }
}
