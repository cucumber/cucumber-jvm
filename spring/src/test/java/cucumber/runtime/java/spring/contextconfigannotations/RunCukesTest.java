package cucumber.runtime.java.spring.contextconfigannotations;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

import cucumber.api.junit.Cucumber;

//@RunWith(Cucumber.class)
@CucumberOptions(features={"classpath:cucumber/runtime/java/spring/springAnnotationBasedConfiguration.feature"})
public class RunCukesTest {

}
