package io.cucumber.spring.contextcaching;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:io/cucumber/spring/contextCaching.feature")
public class RunCucumberTest {

}
