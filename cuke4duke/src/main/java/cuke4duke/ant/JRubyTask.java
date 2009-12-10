package cuke4duke.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment;

import java.io.File;

public class JRubyTask extends Java {
    public JRubyTask() {
        setFailonerror(true);
    }

    @Override
    public void execute() throws BuildException {
        setClassname("org.jruby.Main");
        setGemPath();
        setHome();
        super.execute();
    }

    private void setGemPath() {
        Environment.Variable gemPath = new Environment.Variable();
        gemPath.setKey("GEM_PATH");
        gemPath.setFile(gemHome());
        this.addEnv(gemPath);
    }

    private void setHome() {
        Environment.Variable gemPath = new Environment.Variable();
        gemPath.setKey("HOME");
        gemPath.setFile(gemHome());
        this.addEnv(gemPath);
    }

    private File gemRoot() {
        return new File(getProject().getProperty("jruby.gem.root"));
    }

    private File gemHome() {
        return new File(gemRoot(), ".gem");
    }

    private File binDir() {
        return new File(gemHome(), "bin");
    }
}
