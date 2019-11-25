package io.cucumber.spring.mockbeans;


import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = {"io.cucumber.spring.mockbeans"},
    features = {"classpath:io/cucumber/spring/mockbeans.feature"}
)
public class RunCucumberTest {
}
