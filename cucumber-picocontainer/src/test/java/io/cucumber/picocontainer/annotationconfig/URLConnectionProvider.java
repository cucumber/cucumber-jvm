package io.cucumber.picocontainer.annotationconfig;

import org.picocontainer.injectors.Provider;

import java.net.HttpURLConnection;

public class URLConnectionProvider implements Provider {

    public HttpURLConnection provide() {
        throw new UnsupportedOperationException("Intentionally not supported to detect any premature injection.");
    }

}
