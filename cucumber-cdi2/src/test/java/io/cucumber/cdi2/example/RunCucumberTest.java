package io.cucumber.cdi2.example;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("io.cucumber.cdi2.example")
public class RunCucumberTest {
}
