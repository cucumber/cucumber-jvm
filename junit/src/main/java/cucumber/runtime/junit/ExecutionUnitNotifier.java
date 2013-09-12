package cucumber.runtime.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

/**
 * This class makes sure that an execution unit with a missing step definition is properly
 * marked as ignored. The main reason for being of this class is that in JUnit, a test
 * that is to be ignored can't be started. While for a cuke, we only figure out that a step
 * definition is missing when we get at that step.
 */
public class ExecutionUnitNotifier extends EachTestNotifier {
    
    private final List<Throwable> failures = new ArrayList<Throwable>();
    private boolean ignored;
    
    public ExecutionUnitNotifier(RunNotifier notifier, Description description) {
        super(notifier, description);
    }

    public void addFailure(Throwable targetException) {
        this.failures.add(targetException);
    }

    public void addFailedAssumption(AssumptionViolatedException e) {
        this.failures.add(e);
    }

    public void fireTestFinished() {
        if (ignored) {
            super.fireTestIgnored();
        } else {
            super.fireTestStarted();
            for (Throwable t : failures) {
                if (t instanceof AssumptionViolatedException) {
                    super.addFailedAssumption((AssumptionViolatedException) t);
                } else {
                    super.addFailure(t);
                }
            }
            super.fireTestFinished();
        }
        failures.clear();
        ignored = false;
    }

    public void fireTestStarted() {
    }

    public void fireTestIgnored() {
        ignored = true;
    }
    
}