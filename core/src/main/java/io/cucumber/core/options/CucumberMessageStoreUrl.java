package io.cucumber.core.options;

import io.cucumber.core.exception.CucumberException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class CucumberMessageStoreUrl {
    public static String getPluginString() {
        String url = getUrl();
        try {
            String apiUrl = new URL(new URL(url), "/api/reports").toExternalForm();
            return String.format("message:%s", apiUrl);
        } catch (MalformedURLException e) {
            throw new CucumberException("Bad URL: " + url);
        }
    }

    public static String getUrl() {
        Map<String, String> props = CucumberProperties.create();
        String url = props.get("CUCUMBER_MESSAGE_STORE_URL");
        if (url == null) {
            url = "https://messages.cucumber.io";
        }
        return url;
    }
}
