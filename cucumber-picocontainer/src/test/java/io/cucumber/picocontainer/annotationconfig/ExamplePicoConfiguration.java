package io.cucumber.picocontainer.annotationconfig;

import io.cucumber.picocontainer.CucumberPicoProvider;

@CucumberPicoProvider(providers = ConnectionProvider.class, providerAdapters = DatabaseConnectionProvider.class)
public class ExamplePicoConfiguration {
}
