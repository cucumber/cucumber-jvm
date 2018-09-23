package io.cucumber.examples.java.calculator;

import io.cucumber.core.api.options.CucumberOptions;
import io.cucumber.testng.api.AbstractTestNGCucumberTests;
import org.testng.annotations.DataProvider;

@CucumberOptions(plugin = "json:target/cucumber-report.json")
public class RunCukesTest extends AbstractTestNGCucumberTests {

    @DataProvider(parallel = true)
    @Override
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
