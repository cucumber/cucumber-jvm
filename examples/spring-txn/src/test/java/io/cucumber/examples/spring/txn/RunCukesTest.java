package io.cucumber.examples.spring.txn;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(extraGlue = {"cucumber.api.spring"})
public class RunCukesTest {
}
