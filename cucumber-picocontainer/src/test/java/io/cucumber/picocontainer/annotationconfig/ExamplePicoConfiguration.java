package io.cucumber.picocontainer.annotationconfig;

import io.cucumber.picocontainer.CucumberPicoProvider;
import org.picocontainer.injectors.Provider;

import java.net.HttpURLConnection;
import java.net.URL;

public final class ExamplePicoConfiguration {

    @CucumberPicoProvider
    public static final class NestedUrlProvider implements Provider {
        public URL provide() {
            throw new UnsupportedOperationException("Intentionally not supported to detect any premature injection.");
        }
    }

    @CucumberPicoProvider
    public static final class NestedUrlConnectionProvider implements Provider {
        public HttpURLConnection provide(URL url) {
            throw new UnsupportedOperationException("Intentionally not supported to detect any premature injection.");
        }
    }

}
