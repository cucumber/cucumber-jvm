package io.cucumber.examples.spring.txn;

import io.cucumber.core.api.options.CucumberOptions;
import io.cucumber.junit.api.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(extraGlue = {"io.cucumber.spring.api"})
public class RunCucumberTest {
}
