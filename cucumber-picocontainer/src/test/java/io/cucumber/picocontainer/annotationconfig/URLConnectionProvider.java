package io.cucumber.picocontainer.annotationconfig;

import org.picocontainer.injectors.Provider;

import java.net.HttpURLConnection;
import java.net.URL;

public class URLConnectionProvider implements Provider {

    public HttpURLConnection provide(URL url) {
        throw new UnsupportedOperationException("Intentionally not supported to detect any premature injection.");
    }

}
