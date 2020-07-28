package io.cucumber.core.plugin;

import io.cucumber.core.options.Constants;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CurlOption;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.cucumber.core.options.Constants.PLUGIN_PUBLISH_URL_PROPERTY_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class PublishFormatter implements ConcurrentEventListener {

    /**
     * Where to publishes messages by default
     */
    public static final String DEFAULT_CUCUMBER_MESSAGE_STORE_URL = "https://messages.cucumber.io/api/reports";

    private final MessageFormatter delegate;

    public PublishFormatter() throws IOException {
        this.delegate = new MessageFormatter(makeUrlOutputStream());
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        delegate.setEventPublisher(publisher);
    }

    private static OutputStream makeUrlOutputStream() throws IOException {
        Map<String, String> properties = CucumberProperties.create();

        String url = properties.getOrDefault(PLUGIN_PUBLISH_URL_PROPERTY_NAME, DEFAULT_CUCUMBER_MESSAGE_STORE_URL);
        UrlReporter urlReporter = new UrlReporter(new OutputStreamWriter(System.err, UTF_8));

        List<Map.Entry<String, String>> headers = buildHeadersFromCucumberEnvVars(properties);
        CurlOption curlOption = new CurlOption(URI.create(url), CurlOption.HttpMethod.PUT, headers);
        return new UrlOutputStream(curlOption, urlReporter);
    }

    private static List<Map.Entry<String, String>> buildHeadersFromCucumberEnvVars(Map<String, String> properties) {
        // TODO Use cucumber.plugin.publish as a prefix
        Pattern prefixRegex = Pattern.compile("^CUCUMBER[_.].*", Pattern.CASE_INSENSITIVE);
        return properties.entrySet()
                .stream()
                .filter((entry) -> prefixRegex.matcher(entry.getValue()).matches())
                .map((entry) -> new AbstractMap.SimpleEntry<>(entry.getKey().toLowerCase(), entry.getValue()))
                .collect(Collectors.toList());
    }

}
