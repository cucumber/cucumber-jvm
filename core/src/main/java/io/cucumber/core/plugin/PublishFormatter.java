package io.cucumber.core.plugin;

import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CurlOption;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.IOException;
import java.util.Map;

import static io.cucumber.core.options.Constants.PLUGIN_PUBLISH_URL_PROPERTY_NAME;

public final class PublishFormatter implements ConcurrentEventListener, ColorAware {

    /**
     * Where to publishes messages by default
     */
    public static final String DEFAULT_CUCUMBER_MESSAGE_STORE_URL = "https://messages.cucumber.io/api/reports -X GET";

    private final UrlReporter urlReporter = new UrlReporter(System.err);
    private final MessageFormatter delegate;

    public PublishFormatter() throws IOException {
        this(createCurlOption(null));
    }

    public PublishFormatter(String token) throws IOException {
        this(createCurlOption(token));
    }

    private PublishFormatter(CurlOption curlOption) throws IOException {
        UrlOutputStream outputStream = new UrlOutputStream(curlOption, urlReporter);
        this.delegate = new MessageFormatter(outputStream);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        delegate.setEventPublisher(publisher);
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        urlReporter.setMonochrome(monochrome);
    }

    private static CurlOption createCurlOption(String token) {
        Map<String, String> properties = CucumberProperties.create();
        String url = properties.getOrDefault(PLUGIN_PUBLISH_URL_PROPERTY_NAME, DEFAULT_CUCUMBER_MESSAGE_STORE_URL);
        if (token != null) {
            url = url + String.format(" -H 'Authorization: Bearer %s'", token);
        }
        return CurlOption.parse(url);
    }

}
