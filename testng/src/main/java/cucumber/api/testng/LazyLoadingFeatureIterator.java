package cucumber.api.testng;

import cucumber.runtime.SingleFeatureBuilder;
import cucumber.runtime.io.Resource;

import java.util.*;

/**
 * Created by Stan on 1/23/2016.
 */
public class LazyLoadingFeatureIterator implements Iterator<CucumberFeatureWrapper> {

    private Iterator<Resource> featureResourcesIterator;
    private SingleFeatureBuilder featureBuilder = new SingleFeatureBuilder();

    public LazyLoadingFeatureIterator(Iterator<Resource> featureResourcesIterator) {
        this.featureResourcesIterator = featureResourcesIterator;
    }

    @Override
    public boolean hasNext() {
        return featureResourcesIterator.hasNext();
    }

    @Override
    public CucumberFeatureWrapper next() {
        final Resource featureResource = featureResourcesIterator.next();
        featureBuilder.parse(featureResource, Collections.emptyList());
        return new CucumberFeatureWrapperImpl(featureBuilder.getCurrentCucumberFeature());
    }

    @Override
    public void remove() {
        // nothing to do
    }
}
