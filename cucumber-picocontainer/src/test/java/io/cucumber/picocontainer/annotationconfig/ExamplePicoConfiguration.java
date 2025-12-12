package io.cucumber.picocontainer.annotationconfig;

import io.cucumber.picocontainer.PicoConfiguration;

@PicoConfiguration(providers = ConnectionProvider.class, providerAdapters = DatabaseConnectionProvider.class)
public class ExamplePicoConfiguration {
}
