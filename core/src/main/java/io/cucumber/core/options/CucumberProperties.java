package io.cucumber.core.options;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static io.cucumber.core.options.Constants.CUCUMBER_PROPERTIES_FILE_NAME;

public final class CucumberProperties {

    private static final Logger log = LoggerFactory.getLogger(CucumberProperties.class);

    private CucumberProperties() {

    }

    public static Map<String, String> create() {
        CucumberPropertiesMap fromBundle = new CucumberPropertiesMap(fromPropertiesFile());
        CucumberPropertiesMap fromEnvironmentProperties = new CucumberPropertiesMap(fromBundle, fromEnvironment());
        return new CucumberPropertiesMap(fromEnvironmentProperties, fromSystemProperties());
    }

    public static Map<String, String> fromPropertiesFile() {
        InputStream resourceAsStream = CucumberProperties.class.getResourceAsStream("/" + CUCUMBER_PROPERTIES_FILE_NAME);
        if (resourceAsStream == null) {
            log.debug(CUCUMBER_PROPERTIES_FILE_NAME + " file did not exist");
            return Collections.emptyMap();
        }

        try {
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            return CucumberPropertiesMap.create(properties);
        } catch (IOException e) {
            log.error(CUCUMBER_PROPERTIES_FILE_NAME + " could not be loaded", e);
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> fromSystemProperties() {
        Properties p = System.getProperties();
        return CucumberPropertiesMap.create(p);
    }

    public static Map<String, String> fromEnvironment() {
        Map<String, String> p = System.getenv();
        CucumberPropertiesMap properties = new CucumberPropertiesMap();
        properties.putAll(p);
        return properties;
    }

    static class CucumberPropertiesMap extends HashMap<String, String> {

        private final CucumberPropertiesMap parent;

        CucumberPropertiesMap() {
            this(null);
        }

        CucumberPropertiesMap(CucumberPropertiesMap parent) {
            this(parent, Collections.emptyMap());
        }

        CucumberPropertiesMap(Map<String, String> properties) {
            this(null, properties);
        }

        CucumberPropertiesMap(CucumberPropertiesMap parent, Map<String, String> properties) {
            super(properties);
            this.parent = parent;
        }

        private static CucumberPropertiesMap create(Properties p) {
            CucumberPropertiesMap properties = new CucumberPropertiesMap();
            for (String key : p.stringPropertyNames()) {
                properties.put(key, p.getProperty(key));
            }
            return properties;
        }

        @Override
        public String get(Object key) {
            String exactMatch = super.get(key);
            if (exactMatch != null) {
                return exactMatch;
            }

            if (!(key instanceof String)) {
                return null;
            }

            // Support old skool
            // Not all environments allow properties to contain dots or dashes.
            // So we map the requested property to its underscore case variant.
            String keyString = (String) key;

            String uppercase = keyString
                .replace(".", "_")
                .replace("-", "_")
                .toUpperCase(Locale.ENGLISH);
            String upperCaseMatch = super.get(uppercase);
            if (upperCaseMatch != null) {
                return upperCaseMatch;
            }

            String lowercase = keyString
                .replace(".", "_")
                .replace("-", "_")
                .toLowerCase(Locale.ENGLISH);
            String lowerValue = super.get(lowercase);
            if (lowerValue != null)
                return lowerValue;

            if (parent == null) {
                return null;
            }
            return parent.get(key);
        }

    }
}