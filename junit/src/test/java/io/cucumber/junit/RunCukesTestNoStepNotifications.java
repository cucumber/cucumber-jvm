package io.cucumber.junit;

import io.cucumber.core.api.options.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(junit = "--no-step-notifications")
public class RunCukesTestNoStepNotifications {
}
