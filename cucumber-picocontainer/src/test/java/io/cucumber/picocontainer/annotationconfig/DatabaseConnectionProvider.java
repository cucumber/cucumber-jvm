package io.cucumber.picocontainer.annotationconfig;

import org.picocontainer.injectors.ProviderAdapter;

import java.sql.Connection;

public class DatabaseConnectionProvider extends ProviderAdapter {

    public Connection provide() {
        throw new UnsupportedOperationException("Intentionally not supported to detect any premature injection.");
    }

}
