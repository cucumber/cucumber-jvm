package cucumber.api.testng;

import gherkin.events.PickleEvent;
import io.cucumber.testng.CucumberFeatureWrapper;
import io.cucumber.testng.PickleEventWrapper;
import org.apiguardian.api.API;

/**
 * Glue code for running Cucumber via TestNG.
 *
 * @deprecated use {@code io.cucumber.testng.TestNGCucumberRunner} instead
 */
@API(status = API.Status.MAINTAINED)
@Deprecated
public class TestNGCucumberRunner {

    private final io.cucumber.testng.TestNGCucumberRunner delegate;

    public TestNGCucumberRunner(Class clazz) {
        this.delegate = new io.cucumber.testng.TestNGCucumberRunner(clazz);
    }

    public void runScenario(PickleEvent pickle) throws Throwable {
        delegate.runScenario(pickle);
    }

    public void finish() {
        delegate.finish();
    }

    public Object[][] provideScenarios() {
        return addLegacyWrappers(delegate.provideScenarios());
    }

    private Object[][] addLegacyWrappers(Object[][] objects) {
        for (Object[] row : objects) {
            for (int i = 0; i < row.length; i++) {
                Object element = row[i];
                if (element == null) {
                    continue;
                }

                if (element instanceof PickleEventWrapper) {
                    PickleEventWrapper wrapper = (PickleEventWrapper) element;
                    row[i] = new PickleEventWrapperImpl(wrapper);
                } else if (element instanceof CucumberFeatureWrapper) {
                    CucumberFeatureWrapper wrapper = (CucumberFeatureWrapper) element;
                    row[i] = new CucumberFeatureWrapperImpl(wrapper);
                }
            }
        }

        return objects;
    }
}