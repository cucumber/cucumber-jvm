package io.cucumber.guice.integration;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

/**
 * The Cucumber integration tests use a mixture of annotation and module binding
 * to demonstrate both techniques. The step definition classes are all bound in
 * scenario scope using the @ScenarioScoped annotation. The test object classes
 * are bound using {@link YourModule}.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("io/cucumber/deltaspike")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.cucumber.deltaspike")
public class RunCucumberTest {

}
