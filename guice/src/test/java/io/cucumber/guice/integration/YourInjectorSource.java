package io.cucumber.guice.integration;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import io.cucumber.guice.api.CucumberModules;
import io.cucumber.guice.api.InjectorSource;

public class YourInjectorSource implements InjectorSource {

    @Override
    public Injector getInjector() {
        return Guice.createInjector(Stage.PRODUCTION, CucumberModules.SCENARIO, new YourModule());
    }
}
