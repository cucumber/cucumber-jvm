package io.cucumber.spring.contextconfig;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("io/cucumber/spring/stepdefInjection.feature")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,
        value = "io.cucumber.spring.contextconfig," +
                "io.cucumber.spring.commonglue")
public class RunCucumberTest {

}
