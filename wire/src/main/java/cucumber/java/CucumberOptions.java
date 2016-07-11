package cucumber.java;

public class CucumberOptions {
    private static CucumberOptions options = new CucumberOptions();

    private int port = 3902;
    private boolean verbose = false;

    private CucumberOptions() {}

    public static CucumberOptions getOptions() {
        return options;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public int getPort() {
        return port;
    }

    public boolean isVerbose() {
        return verbose;
    }
}
