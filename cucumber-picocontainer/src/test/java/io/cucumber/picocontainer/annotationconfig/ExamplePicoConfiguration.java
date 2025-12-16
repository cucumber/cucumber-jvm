package io.cucumber.picocontainer.annotationconfig;

import io.cucumber.picocontainer.CucumberPicoProvider;
import org.picocontainer.injectors.Provider;

import java.net.URL;

@CucumberPicoProvider(providers = { URLConnectionProvider.class, DatabaseConnectionProvider.class })
public class ExamplePicoConfiguration {

    @CucumberPicoProvider
    public static class NestedUrlProvider implements Provider {
        public URL provide() {
            throw new UnsupportedOperationException("Intentionally not supported to detect any premature injection.");
        }
    }

}
