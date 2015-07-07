package cucumber.runtime;

import java.util.HashMap;
import java.util.Map;
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
    public static final Env INSTANCE = new Env("cucumber");
    private final Map<String, String> map = new HashMap<String, String>();

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
        if (bundleName != null) {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
                for (String key : bundle.keySet()) {
                    put(key, bundle.getString(key));
                }
            } catch (MissingResourceException ignore) {
            }
        }

        if (properties != null) {
            for (String key : properties.stringPropertyNames()) {
                put(key, properties.getProperty(key));
            }
        }

        Map<String, String> env = System.getenv();
        for (String key : env.keySet()) {
            put(key, env.get(key));
        }
    }

    private void put(String key, String string) {
        map.put(key, string);
        // Support old skool
        map.put(key.replace('.', '_').toUpperCase(), string);
        map.put(key.replace('_', '.').toLowerCase(), string);
    }

    public String get(String key) {
        return map.get(key);
    }

    public String get(String key, String defaultValue) {
        String result = get(key);
        return result != null ? result : defaultValue;
    }
}
