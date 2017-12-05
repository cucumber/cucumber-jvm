package cucumber.examples.java.calculator;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.jupiter.CucumberExtension;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.stream.Stream;

@ExtendWith(CucumberExtension.class)
@CucumberOptions(plugin = "json:target/cucumber-report.json")
public class RunCukesTest {

    @TestFactory
    public Stream<DynamicContainer> runAllCucumberScenarios(Stream<DynamicContainer> scenarios) {
        return scenarios;
    }
}
