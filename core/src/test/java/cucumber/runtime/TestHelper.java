package cucumber.runtime;

import cucumber.io.Resource;
import cucumber.runtime.model.CucumberFeature;

import org.junit.Ignore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

@Ignore
public class TestHelper {
    static CucumberFeature feature(final String path, final String source) throws IOException {
        ArrayList<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
        FeatureBuilder featureBuilder = new FeatureBuilder(cucumberFeatures);
        featureBuilder.parse(new Resource() {
            @Override
            public String getPath() {
                return path;
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
            public String getClassName() {
                throw new UnsupportedOperationException();
            }
        }, new ArrayList<Object>());
        return cucumberFeatures.get(0);
    }
}
