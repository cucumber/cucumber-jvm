package io.cucumber.examples.calculator;

import io.cucumber.junit.platform.engine.Cucumber;

/**
 * Work around. Surefire does not use JUnits Test Engine discovery
 * functionality. Alternatively execute the the
 * org.junit.platform.console.ConsoleLauncher with the maven-antrun-plugin.
 */
@Cucumber
public class RunCucumberTest {

}
