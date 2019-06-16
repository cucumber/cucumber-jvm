package io.cucumber.needle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instantiates new java object by default constructor
 */
enum CreateInstanceByDefaultConstructor {
    /**
     * Singleton
     */
    INSTANCE;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    final <T> T apply(final Class<T> type) {
        try {
            final T newInstance = type.getConstructor().newInstance();
            logger.debug("newInstance by DefaultConstructor: {}", newInstance);
            return newInstance;
        } catch (final Exception e) {
            throw new IllegalStateException("Can not instantiate Instance by Default Constructor.", e);
        }
    }

}
