package cucumber.runtime;

import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Looks up values in the following order:
 * <ol>
 * <li>Environment variable</li>
 * <li>System property</li>
 * <li>Resource bundle</li>
 * </ol>
 */
public class Env {
    private final String bundleName;
    private final Properties properties;

    public Env() {
        this(null, System.getProperties());
    }

    public Env(String bundleName) {
        this(bundleName, System.getProperties());
    }

    public Env(Properties properties) {
        this(null, properties);
    }

    public Env(String bundleName, Properties properties) {
        this.bundleName = bundleName;
        this.properties = properties;
    }

    public String get(String key) {
        String result = getFromEnvironment(key);
        if (result == null) {
            result = getFromProperty(key);
            if (result == null && bundleName != null) {
                result = getFromBundle(key);
            }
        }
        return result;
    }

    private String getFromEnvironment(String key) {
        String value = System.getenv(asEnvKey(key));
        if (value == null) {
            value = System.getenv(asPropertyKey(key));
        }
        return value;
    }

    private String getFromProperty(String key) {
        String value = properties.getProperty(asEnvKey(key));
        if (value == null) {
            value = properties.getProperty(asPropertyKey(key));
        }
        return value;
    }

    private String getFromBundle(String key) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
            try {
                return bundle.getString(asEnvKey(key));
            } catch (MissingResourceException stringNotFound) {
                try {
                    return bundle.getString(asPropertyKey(key));
                } catch (MissingResourceException ignoreStringNotFound) {
                    return bundle.getString(asPropertyKey(key));
                }
            }
        } catch (MissingResourceException ignoreBundleNotFound) {
            return null;
        }
    }

    public String get(String key, String defaultValue) {
        String result = get(key);
        return result != null ? result : defaultValue;
    }

    private static String asEnvKey(String key) {
        return key.replace('.', '_').toUpperCase();
    }

    private static String asPropertyKey(String key) {
        return key.replace('_', '.').toLowerCase();
    }
}
