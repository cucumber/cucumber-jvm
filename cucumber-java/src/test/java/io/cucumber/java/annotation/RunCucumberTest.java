package io.cucumber.java.annotation;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("io.cucumber.java.annotation")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.cucumber.java.annotation")
public class RunCucumberTest {

}
