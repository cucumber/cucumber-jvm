package io.cucumber.junit;

import org.junit.Test;

public class SanityTest {
    @Test
    public void reports_events_correctly_with_cucumber_runner() {
        SanityChecker.run(RunCukesTest.class);
    }

    @Test
    public void reports_events_correctly_with_junit_runner() {
        SanityChecker.run(RunCukesTest.class);
    }

    @Test
    public void reports_events_correctly_with_no_step_notifications() {
        SanityChecker.run(RunCukesTestNoStepNotifications.class);
    }
}
