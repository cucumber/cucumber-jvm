package cucumber.examples.java.scan;

import cucumber.junit.Cucumber;
import cucumber.junit.Feature;
import org.junit.runner.RunWith;

/**
 * This test class tells JUnit to run a particular feature with Cucumber.
 * 
 * Step definitions will be scanned underneath both this class' package, and
 * underneath cucumber.examples.java.calculator. 
 */
@RunWith(Cucumber.class)
@Feature(value = "basic_arithmetic.feature", packages = {"cucumber.examples.java.calculator"})
public class basic_arithmetic_with_scan_Test {
}
