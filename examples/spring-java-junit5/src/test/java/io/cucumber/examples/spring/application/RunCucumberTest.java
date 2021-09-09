package io.cucumber.examples.spring.application;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("io/cucumber/examples/spring/application")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.cucumber.examples.spring.application")
public class RunCucumberTest {
}
