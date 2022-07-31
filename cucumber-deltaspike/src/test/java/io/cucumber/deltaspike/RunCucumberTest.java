package io.cucumber.deltaspike;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("io/cucumber/deltaspike")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.cucumber.deltaspike")
public class RunCucumberTest {

}
