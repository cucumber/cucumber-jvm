package cuke4duke.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Environment;

import java.io.File;

public class CucumberTask extends JRubyTask {
    private String args = "";

    public void execute() throws BuildException {
        createArg().setValue("-r");
        createArg().setValue("cuke4duke");
        setCucumberBin();
        getCommandLine().createArgument().setLine(args);
        super.execute();
    }

    private void setCucumberBin() {
        String cucumberBinProperty = System.getProperty("cucumber.bin");
        if(cucumberBinProperty != null) {
            File cucumberBin = new File(cucumberBinProperty);
            createArg().setValue("-I");
            createArg().setFile(new File(cucumberBin.getParentFile().getParentFile(), "lib"));
            createArg().setFile(cucumberBin);
        } else {
            createArg().setFile(getGemBinFile("cucumber"));
        }
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
