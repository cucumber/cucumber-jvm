package io.cucumber.picocontainer.annotationconfig;

import io.cucumber.picocontainer.CucumberPicoProvider;
import org.picocontainer.injectors.Provider;

import java.net.URI;
import java.net.URL;

@CucumberPicoProvider
public final class UrlToUriProvider implements Provider {

    public URI provide(URL url) {
        throw new UnsupportedOperationException("Intentionally not supported to detect any premature injection.");
    }

}
