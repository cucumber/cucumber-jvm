package cucumber.runtime.junit;

import cucumber.junit.Cucumber;
import cucumber.junit.Feature;
import org.junit.runner.RunWith;

/**
 * In order to run a Cucumber Feature from JUnit - all you need is an empty class annotated like below.
 * The @Feature annotation is not required if the test class is named the same as the feature file and
 * in the same package.
 * <p/>
 * You can write this stub class by hand, but Cucumber will also have a tool that can generate them
 * from .feature sources.
 */
@RunWith(Cucumber.class)
@Feature("cucumber_runner.feature")
public class cucumber_runner_Test {
}
