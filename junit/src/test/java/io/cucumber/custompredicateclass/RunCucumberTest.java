package io.cucumber.custompredicateclass;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(customPredicateClass = FilterOutPredicate.class)
public class RunCucumberTest {

}
