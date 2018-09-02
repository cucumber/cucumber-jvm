package io.cucumber.spring.metaconfig.general;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = {"cucumber.runtime.java.spring.metaconfig.general",
                "cucumber.runtime.java.spring.commonglue",
                "cucumber.api.spring"},
        features = {"classpath:io/cucumber/spring/springBeanInjectionWithMetaConfiguration.feature"})
public class RunCukesTest {
}
