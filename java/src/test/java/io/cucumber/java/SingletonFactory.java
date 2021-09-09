package io.cucumber.java;

import io.cucumber.core.backend.ObjectFactory;

class SingletonFactory implements ObjectFactory {

    private Object singleton;

    public SingletonFactory() {
        this(null);
    }

    public SingletonFactory(Object singleton) {
        this.singleton = singleton;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean addClass(Class<?> clazz) {
        return true;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        if (singleton == null) {
            throw new IllegalStateException("No object is set");
        }
        return type.cast(singleton);
    }

    public void setInstance(Object o) {
        singleton = o;
    }

}
