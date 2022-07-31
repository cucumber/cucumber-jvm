package io.cucumber.guice.factory;

import com.google.inject.Injector;
import io.cucumber.guice.InjectorSource;

public class SecondInjectorSource implements InjectorSource {
    @Override
    public Injector getInjector() {
        return null;
    }
}
