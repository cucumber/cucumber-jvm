package io.cucumber.core.plugin;

import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CurlOption;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PublishFormatter extends MessageFormatter {
    /**
     * When this environment variable is defined, the publish formatter is
     * automatically enabled, even if --publish is not specified
     */
    public static final String CUCUMBER_REPORTS_TOKEN = "CUCUMBER_REPORTS_TOKEN";

    /**
     * Overrides the URL where messages are published
     */
    public static final String CUCUMBER_MESSAGE_STORE_URL = "CUCUMBER_MESSAGE_STORE_URL";

    /**
     * Where to publishes messages by default
     */
    public static final String DEFAULT_CUCUMBER_MESSAGE_STORE_URL = "https://messages.cucumber.io/api/reports";

    private static final Map<String, String> CUCUMBER_PROPERTIES = CucumberProperties.create();

    public PublishFormatter() throws IOException {
        super(makeUrlOutputStream());
    }

    private static OutputStream makeUrlOutputStream() throws IOException {
        String url = CUCUMBER_PROPERTIES.get(CUCUMBER_MESSAGE_STORE_URL);
        if (url == null) {
            url = DEFAULT_CUCUMBER_MESSAGE_STORE_URL;
        }
        UrlReporter urlReporter = new UrlReporter(new OutputStreamWriter(System.err, UTF_8));

        List<Map.Entry<String, String>> headers = buildHeadersFromCucumberEnvVars();
        CurlOption curlOption = new CurlOption(URI.create(url), CurlOption.HttpMethod.PUT, headers);
        return new UrlOutputStream(curlOption, urlReporter);
    }

    private static List<Map.Entry<String, String>> buildHeadersFromCucumberEnvVars() {
        Pattern prefixRegex = Pattern.compile("^CUCUMBER[_.].*", Pattern.CASE_INSENSITIVE);
        return CUCUMBER_PROPERTIES.entrySet()
                .stream()
                .filter((entry) -> prefixRegex.matcher(entry.getValue()).matches())
                .map((entry) -> new AbstractMap.SimpleEntry<>(entry.getKey().toLowerCase(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public static boolean isEnabledWithEnvironmentVariable() {
        // Can't use containsKey(key) here because only get(key) has been
        // overridden to match upper/lower and ./_ separators
        return CUCUMBER_PROPERTIES.get(CUCUMBER_REPORTS_TOKEN) != null;
    }
}
