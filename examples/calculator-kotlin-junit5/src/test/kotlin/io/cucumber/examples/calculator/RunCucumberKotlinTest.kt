package io.cucumber.examples.calculator

import io.cucumber.junit.platform.engine.Constants
import org.junit.platform.suite.api.Suite
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectPackages
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.SelectClasspathResource


@Suite
@IncludeEngines("cucumber")
@SelectPackages("io.cucumber.examples.calculator")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "io.cucumber.examples.calculator")
@SelectClasspathResource("io.cucumber.examples.calculator")
class RunCucumberKotlinTest
