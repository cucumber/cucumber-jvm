package cucumber.api.testng;

import cucumber.runtime.CucumberException;
import cucumber.runtime.model.CucumberFeature;

public class CucumberExceptionWrapper implements CucumberFeatureWrapper {
    private CucumberException exception;

    public CucumberExceptionWrapper(CucumberException e) {
        this.exception = e;
    }

    @Override
    public CucumberFeature getCucumberFeature() {
        throw this.exception;
    }

}
