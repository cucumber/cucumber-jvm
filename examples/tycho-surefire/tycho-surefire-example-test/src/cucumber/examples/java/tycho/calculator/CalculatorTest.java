package cucumber.examples.java.tycho.calculator;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.java.runtime.osgi.CucumberOSGi;

@RunWith(CucumberOSGi.class)
@CucumberOptions(features = "features")
public class CalculatorTest {

}
