package cucumber.runtime.java.picocontainer;

import cucumber.runtime.junit.SanityChecker;
import org.junit.Ignore;
import org.junit.Test;

public class SanityTest {
    @Test
    public void reports_events_correctly_with_cucumber_runner() {
        SanityChecker.run(RunCukesTest.class, true);
    }
}
