package io.cucumber.spring.metaconfig.general;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        glue = {
                "io.cucumber.spring.metaconfig.general",
                "io.cucumber.spring.commonglue",
                "cucumber.api.spring"
        },
        features = "classpath:io/cucumber/spring/springBeanInjectionWithMetaConfiguration.feature")
public class RunCucumberTest {

}
