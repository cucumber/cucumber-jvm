package io.cucumber.spring.metaconfig.general;

import io.cucumber.core.api.options.CucumberOptions;
import io.cucumber.junit.api.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = {"cucumber.runtime.java.spring.metaconfig.general",
                "cucumber.runtime.java.spring.commonglue",
                "cucumber.api.spring"},
        features = {"classpath:io/cucumber/spring/springBeanInjectionWithMetaConfiguration.feature"})
public class RunCukesTest {
}
