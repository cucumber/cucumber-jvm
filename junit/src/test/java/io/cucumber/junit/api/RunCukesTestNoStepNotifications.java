package io.cucumber.junit.api;

import io.cucumber.core.api.options.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(junit = "--no-step-notifications")
public class RunCukesTestNoStepNotifications {
}
