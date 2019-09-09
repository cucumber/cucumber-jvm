package io.cucumber.needle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Null safe Resource Loader. If ResourceBundle does not exist, an empty Bundle is returned.
 */
enum LoadResourceBundle {
    INSTANCE;

    public static final ResourceBundle EMPTY_RESOURCE_BUNDLE = new ResourceBundle() {

        @Override
        public Enumeration<String> getKeys() {
            return new Enumeration<String>() {

                @Override
                public boolean hasMoreElements() {
                    return false;
                }

                @Override
                public String nextElement() {
                    return null;
                }
            };
        }

        @Override
        protected Object handleGetObject(final String key) {
            return "";
        }
    };

    private final Logger logger = LoggerFactory.getLogger(LoadResourceBundle.class);

    public final ResourceBundle apply(final String resourceName) {
        if (resourceName == null || "".equals(resourceName.trim())) {
            throw new IllegalArgumentException("resourceName must not be null or empty!");
        }

        try {
            return ResourceBundle.getBundle(resourceName);
        } catch (final MissingResourceException e) {
            logger.warn(e.getMessage());
            return EMPTY_RESOURCE_BUNDLE;
        }
    }

}
