package io.cucumber.spring.webappconfig;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("io/cucumber/spring/springWebContextInjection.feature")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.cucumber.spring.webappconfig")
public class RunCucumberTest {

}
