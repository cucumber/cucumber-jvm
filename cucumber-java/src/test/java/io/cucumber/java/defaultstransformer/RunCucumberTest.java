package io.cucumber.java.defaultstransformer;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("io.cucumber.java.defaultstransformer")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.cucumber.java.defaultstransformer")
public class RunCucumberTest {

}
