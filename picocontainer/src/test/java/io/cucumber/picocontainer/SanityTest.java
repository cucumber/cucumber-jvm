package io.cucumber.picocontainer;

import org.junit.jupiter.api.Test;

public class SanityTest {

    @Test
    public void reports_events_correctly_with_cucumber_runner() {
        SanityChecker.run(RunCucumberTest.class, true);
    }

}
