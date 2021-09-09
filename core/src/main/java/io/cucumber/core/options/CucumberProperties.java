package io.cucumber.core.options;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static io.cucumber.core.options.Constants.CUCUMBER_PROPERTIES_FILE_NAME;
import static java.util.Objects.requireNonNull;

/**
 * Store properties.
 * <p>
 * Cucumber can read properties from file, environment or system properties.
 * <p>
 * Cucumber properties are formatted using kebab-case. E.g.
 * {@code cucumber.snippet-type}. To facilitate environments that do no support
 * kebab case properties can also be formatted (in order of preference) using
 * upper snake case e.g. {@code CUCUMBER_SNIPPET_TYPE} or lower snake case e.g.
 * {@code cucumber_snippet_type}.
 */
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
        InputStream resourceAsStream = CucumberProperties.class
                .getResourceAsStream("/" + CUCUMBER_PROPERTIES_FILE_NAME);
        if (resourceAsStream == null) {
            log.debug(() -> CUCUMBER_PROPERTIES_FILE_NAME + " file did not exist");
            return Collections.emptyMap();
        }

        try {
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            return CucumberPropertiesMap.create(properties);
        } catch (IOException e) {
            log.error(e, () -> CUCUMBER_PROPERTIES_FILE_NAME + " could not be loaded");
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> fromEnvironment() {
        Map<String, String> p = System.getenv();
        return new CucumberPropertiesMap(p);
    }

    public static Map<String, String> fromSystemProperties() {
        Properties p = System.getProperties();
        return CucumberPropertiesMap.create(p);
    }

    static class CucumberPropertiesMap extends AbstractMap<String, String> {

        private final CucumberPropertiesMap parent;
        private final Map<String, String> delegate;

        CucumberPropertiesMap(CucumberPropertiesMap parent, Map<String, String> delegate) {
            this.delegate = requireNonNull(delegate);
            this.parent = parent;
        }

        CucumberPropertiesMap(Map<String, String> delegate) {
            this(null, delegate);
        }

        @Override
        public Set<Entry<String, String>> entrySet() {
            return delegate.entrySet();
        }

        private static CucumberPropertiesMap create(Properties p) {
            Map<String, String> copy = new HashMap<>();
            p.stringPropertyNames().forEach(s -> copy.put(s, p.getProperty(s)));
            return new CucumberPropertiesMap(copy);
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
