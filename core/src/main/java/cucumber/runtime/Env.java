package cucumber.runtime;

import java.util.MissingResourceException;
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

    public Env(String bundleName) {
        this.bundleName = bundleName;
    }

    public String get(String key) {
        String value = System.getenv(key);
        if (value == null) {
            value = System.getProperty(key);
            if (value == null) {
                try {
                    value = ResourceBundle.getBundle(bundleName).getString(key);
                } catch (MissingResourceException ignore) {
                }
            }
        }
        return value;
    }
}
