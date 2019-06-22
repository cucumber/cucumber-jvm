package io.cucumber.junit;

import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(junit = "--no-step-notifications")
public class RunCucumberTestNoStepNotifications {
}
