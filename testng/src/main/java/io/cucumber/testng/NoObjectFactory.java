package io.cucumber.testng;

import io.cucumber.core.backend.ObjectFactory;

/**
 * This object factory does nothing. It is solely needed for marking purposes.
 */
final class NoObjectFactory implements ObjectFactory {

    private NoObjectFactory() {
        // No need for instantiation
    }

    @Override
    public boolean addClass(Class<?> glueClass) {
        return false;
    }

    @Override
    public <T> T getInstance(Class<T> glueClass) {
        return null;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

}
