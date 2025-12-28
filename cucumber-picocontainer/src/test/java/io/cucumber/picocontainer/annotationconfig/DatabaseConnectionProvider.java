package io.cucumber.picocontainer.annotationconfig;

import io.cucumber.picocontainer.CucumberPicoProvider;
import org.picocontainer.injectors.ProviderAdapter;

import java.sql.Connection;

@CucumberPicoProvider
public class DatabaseConnectionProvider extends ProviderAdapter {

    public Connection provide() {
        throw new UnsupportedOperationException("Intentionally not supported to detect any premature injection.");
    }

}
