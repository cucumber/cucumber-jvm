package cucumber.core2;

import java.util.ArrayList;
import java.util.List;

public class TestCase {
    private final List<TestStep> testSteps = new ArrayList<TestStep>();

    public void add(TestStep testStep) {
        testSteps.add(testStep);
    }

    public void run() {
        for (TestStep testStep : testSteps) {
            testStep.run();
        }
    }
}
