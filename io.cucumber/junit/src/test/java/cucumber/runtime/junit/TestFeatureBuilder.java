package cucumber.runtime.junit;

import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.io.Resource;
import cucumber.runtime.model.CucumberFeature;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class TestFeatureBuilder {

    private TestFeatureBuilder() {
    }

    static CucumberFeature feature(final String path, final String source) throws IOException {
        ArrayList<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
        FeatureBuilder featureBuilder = new FeatureBuilder(cucumberFeatures);
        featureBuilder.parse(new Resource() {
            @Override
            public String getPath() {
                return path;
            }

            @Override
            public String getAbsolutePath() {
                throw new UnsupportedOperationException();
            }

            @Override
            public InputStream getInputStream() {
                try {
                    return new ByteArrayInputStream(source.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getClassName(String extension) {
                throw new UnsupportedOperationException();
            }
        }, new ArrayList<Object>());
        featureBuilder.close();
        return cucumberFeatures.get(0);
    }

}
