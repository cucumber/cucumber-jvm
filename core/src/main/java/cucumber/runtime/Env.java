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
        String value = System.getenv(asEnvKey(key));
        if (value == null) {
            value = properties.getProperty(key);
            if (value == null && bundleName != null) {
                try {
                    value = ResourceBundle.getBundle(bundleName).getString(key);
                } catch (MissingResourceException ignore) {
                }
            }
        }
        return value;
    }

    private static String asEnvKey(String key) {
        return key.replace('.', '_').toUpperCase();
    }
}
