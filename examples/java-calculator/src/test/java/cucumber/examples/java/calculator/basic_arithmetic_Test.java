package cucumber.examples.java.calculator;

import cucumber.junit.Cucumber;
import cucumber.junit.Feature;
import org.junit.runner.RunWith;

/**
 * This test class tells JUnit to run a particular feature with Cucumber.
 * The package of this class is significant - step definitions will be scanned
 * underneath this class' package.
 */
@RunWith(Cucumber.class)
@Feature("basic_arithmetic.feature")
public class basic_arithmetic_Test {}
