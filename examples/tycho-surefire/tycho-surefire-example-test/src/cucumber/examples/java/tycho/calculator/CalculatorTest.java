package cucumber.examples.java.tycho.calculator;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.osgi.CucumberOSGi;

@RunWith(CucumberOSGi.class)
@CucumberOptions(features = "features")
public class CalculatorTest {

}
