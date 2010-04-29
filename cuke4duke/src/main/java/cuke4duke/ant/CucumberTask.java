package cuke4duke.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Environment;

import java.io.File;

public class CucumberTask extends JRubyTask {
    private String args = "";
    private File bin;

    public void execute() throws BuildException {
        createArg().setFile(getCuke4dukeBinFile());
        getCommandLine().createArgument().setLine(args);
        super.execute();
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public void setBin(File bin) {
        this.bin = bin;
    }

    public void setObjectFactory(String name) {
        Environment.Variable objectFactory = new Environment.Variable();
        objectFactory.setKey("cuke4duke.objectFactory");
        objectFactory.setValue("cuke4duke.internal.jvmclass." + name.substring(0, 1).toUpperCase() + name.substring(1) + "Factory");
        this.addSysproperty(objectFactory);
    }

    private File getCuke4dukeBinFile() {
        if (bin != null) {
            return bin;
        } else if (System.getProperty("cuke4duke.bin") != null) {
            return new File(System.getProperty("cuke4duke.bin"));
        } else {
            return new File(getBinDir(), "cuke4duke");
        }
    }
}
