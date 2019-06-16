package cucumber.api.testng;

/**
 * @deprecated use {@link io.cucumber.testng.CucumberFeatureWrapper} instead
 */
@Deprecated
class CucumberFeatureWrapperImpl implements CucumberFeatureWrapper {
    private final io.cucumber.testng.CucumberFeatureWrapper delegate;

    CucumberFeatureWrapperImpl(io.cucumber.testng.CucumberFeatureWrapper cucumberFeature) {
        this.delegate = cucumberFeature;
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
