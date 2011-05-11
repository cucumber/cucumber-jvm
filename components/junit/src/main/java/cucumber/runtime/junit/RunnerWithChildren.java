package cucumber.runtime.junit;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;

public class RunnerWithChildren extends ParentRunner<ParentRunner> {
    private final String name;
    private final List<ParentRunner> children;

    public RunnerWithChildren(String name, List<ParentRunner> children) throws InitializationError {
        super(null);
        this.name = name;
        this.children = children;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected List<ParentRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(ParentRunner runner) {
        return runner.getDescription();
    }

    @Override
    protected void runChild(ParentRunner runner, RunNotifier notifier) {
        runner.run(notifier);
    }
}
