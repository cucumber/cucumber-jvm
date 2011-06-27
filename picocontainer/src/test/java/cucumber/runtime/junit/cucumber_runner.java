package cucumber.runtime.junit;

import cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * In order to run a Cucumber Feature from JUnit - all you need is an empty class annotated like below.
 * The class must be named the same as the feature file and be in the same package.
 * 
 * You can write this stub class by hand, but Cucumber will also have a tool that can generate them
 * from .feature sources.
 */
@RunWith(Cucumber.class)
public class cucumber_runner {}
