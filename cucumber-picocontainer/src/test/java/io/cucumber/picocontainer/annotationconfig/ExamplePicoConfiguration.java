package io.cucumber.picocontainer.annotationconfig;

import io.cucumber.picocontainer.CucumberPicoProvider;

@CucumberPicoProvider(providers = { URLConnectionProvider.class, DatabaseConnectionProvider.class })
public class ExamplePicoConfiguration {
}
