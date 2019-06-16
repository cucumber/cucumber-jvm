package io.cucumber.junit;

import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(junit = "--no-step-notifications")
public class RunCukesTestNoStepNotifications {
}
