package cucumber.examples.spring.txn;

import cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@Cucumber.Options(glue = {"cucumber.examples.spring.txn", "cucumber.runtime.java.spring.hooks"})
public class RunCukesTest {
}
