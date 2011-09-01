package cucumber.runtime.java.picocontainer;

import cucumber.junit.Cucumber;
import cucumber.junit.Feature;
import org.junit.runner.RunWith;

/**
 * In order to run a Cucumber Feature from JUnit - all you need is an empty class annotated like below.
 * The @Feature annotation is not required if the test class is named the same as the feature file and
 * in the same package.
 */
@RunWith(Cucumber.class)
//@Feature(value="cukes.feature", tags={"@foo"})
@Feature(value = "cukes.feature", lines = {8})
//@Feature("cukes.feature")
public class cukes_Test {
}
