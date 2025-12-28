package io.cucumber.picocontainer.annotationconfig;

import io.cucumber.picocontainer.CucumberPicoProvider;
import org.picocontainer.injectors.Provider;

import java.net.HttpURLConnection;
import java.net.URL;

public class ExamplePicoConfiguration {

    @CucumberPicoProvider
    public static class NestedUrlProvider implements Provider {
        public URL provide() {
            throw new UnsupportedOperationException("Intentionally not supported to detect any premature injection.");
        }
    }

    @CucumberPicoProvider
    public static class NestedUrlConnectionProvider implements Provider {
        public HttpURLConnection provide(URL url) {
            throw new UnsupportedOperationException("Intentionally not supported to detect any premature injection.");
        }
    }

}
