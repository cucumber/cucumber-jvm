package cucumber.examples.java.helloworld;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
// Run scenarios tagged with @foo OR @bar. Can be overridden with e.g. -Dcucumber.options="--tags @foo"
// on the command line.
@Cucumber.Options(tags = {"@foo,@bar"}, format = {"html:target/cucumber-html-report", "json-pretty:target/cucumber-json-report.json"})
public class RunCukesTest {
}
