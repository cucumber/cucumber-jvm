package io.cucumber.guice.factory;

import com.google.inject.Injector;
import io.cucumber.guice.InjectorSource;

public final class SecondInjectorSource implements InjectorSource {
    @Override
    @SuppressWarnings("NullAway")
    public Injector getInjector() {
        return null;
    }
}
