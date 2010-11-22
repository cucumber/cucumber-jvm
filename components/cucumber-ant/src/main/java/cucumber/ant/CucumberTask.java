package cucumber.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment;

// TODO: Rewrite this task!
public class CucumberTask extends Java {
    private String args = "";

    public void execute() throws BuildException {
        super.execute();
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public void setObjectFactory(String name) {
        Environment.Variable objectFactory = new Environment.Variable();
        objectFactory.setKey("cuke4duke.objectFactory");
        objectFactory.setValue("cuke4duke.internal.jvmclass." + name.substring(0, 1).toUpperCase() + name.substring(1) + "Factory");
        this.addSysproperty(objectFactory);
    }
}
