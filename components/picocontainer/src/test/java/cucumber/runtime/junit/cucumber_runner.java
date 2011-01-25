package cucumber.runtime.junit;

import org.junit.runner.RunWith;

/**
 * In order to run a Cucumber Feature from JUnit - all you need is an empty class annotated like below.
 * The class must be named the same as the feature file and be in the same package.
 */
@RunWith(FeatureRunner.class)
public class cucumber_runner {}
