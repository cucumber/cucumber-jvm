package cucumber.api.testng;

import org.apiguardian.api.API;

/**
 * The only purpose of this interface is to be able to provide a custom
 * <pre>toString()</pre>, making TestNG reports look more descriptive.
 *
 * @see AbstractTestNGCucumberTests#runScenario(cucumber.api.testng.PickleEventWrapper, cucumber.api.testng.CucumberFeatureWrapper)
 * @deprecated use {@link io.cucumber.testng.CucumberFeatureWrapper} instead.
 */
@Deprecated
@API(status = API.Status.MAINTAINED)
public interface CucumberFeatureWrapper extends io.cucumber.testng.CucumberFeatureWrapper {

}
