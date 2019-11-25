package io.cucumber.picocontainer;

import org.junit.jupiter.api.Test;

class SanityTest {

    @Test
    void reports_events_correctly_with_cucumber_runner() {
        SanityChecker.run(RunCucumberTest.class, true);
    }

}
