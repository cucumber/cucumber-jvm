package io.cucumber.junit;

import org.junit.jupiter.api.Test;

class SanityTest {

    @Test
    void reports_events_correctly_with_cucumber_runner() {
        SanityChecker.run(RunCucumberTest.class);
    }

    @Test
    void reports_events_correctly_with_junit_runner() {
        SanityChecker.run(RunCucumberTest.class);
    }

    @Test
    void reports_events_correctly_with_no_step_notifications() {
        SanityChecker.run(RunCucumberTestWithStepNotifications.class);
    }

}
