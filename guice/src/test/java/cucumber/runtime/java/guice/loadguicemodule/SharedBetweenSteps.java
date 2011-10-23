package cucumber.runtime.java.guice.loadguicemodule;

public class SharedBetweenSteps {
    public boolean visited = false;

    public void visit() {
        visited = true;
    }
}