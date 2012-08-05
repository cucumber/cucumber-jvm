package cucumber.examples.java.helloworld;

import java.io.IOException;

import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import cucumber.junit.Cucumber.Options;
import cucumber.runtime.java.ObjectFactoryHolder;
import cucumber.testng.TestFactory;

@Listeners()
@Options(format = {"pretty", "html:target/cucumber-html-report"}, features={"classpath:"})
@Test
public class RunCukesTest {
	
	@Factory()
	public Object[] getScenarioTests(){
		try {
  		  return new TestFactory().getCucumberTests(RunCukesTest.class);
		} catch (IOException ioe){
		  ioe.printStackTrace();
		  throw new RuntimeException(ioe);
		}
	}
}
