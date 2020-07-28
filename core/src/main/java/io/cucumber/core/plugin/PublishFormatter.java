package io.cucumber.core.plugin;

import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CurlOption;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.cucumber.core.options.Constants.PLUGIN_PUBLISH_URL_PROPERTY_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class PublishFormatter implements ConcurrentEventListener, ColorAware {

    /**
     * Where to publishes messages by default
     */
    public static final String DEFAULT_CUCUMBER_MESSAGE_STORE_URL = "https://messages.cucumber.io/api/reports";

    private final UrlReporter urlReporter = new UrlReporter(new OutputStreamWriter(System.err, UTF_8));
    private final MessageFormatter delegate;

    public PublishFormatter() throws IOException {
        this.delegate = new MessageFormatter(makeUrlOutputStream(null));
    }

    public PublishFormatter(String token) throws IOException {
        this.delegate = new MessageFormatter(makeUrlOutputStream(token));
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        delegate.setEventPublisher(publisher);
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        urlReporter.setMonochrome(monochrome);
    }
    private OutputStream makeUrlOutputStream(String token) throws IOException {
        Map<String, String> properties = CucumberProperties.create();
        // TODO: Move to properties parsing
        String url = properties.getOrDefault(PLUGIN_PUBLISH_URL_PROPERTY_NAME, DEFAULT_CUCUMBER_MESSAGE_STORE_URL);
        List<Map.Entry<String, String>> headers = buildHeaders(token);
        // TODO: Nice constructor
        CurlOption curlOption = new CurlOption(URI.create(url), CurlOption.HttpMethod.PUT, headers);
        return new UrlOutputStream(curlOption, urlReporter);
    }

    private static List<Map.Entry<String, String>> buildHeaders(String token) {
        if (token == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(Collections.singletonMap("Authorization", "Bearer " + token).entrySet());
    }


}
