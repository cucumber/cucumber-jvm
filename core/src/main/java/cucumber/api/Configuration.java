package cucumber.api;

import java.util.Locale;

public interface Configuration {

    Locale locale();

    void configureTypeRegistry(TypeRegistry typeRegistry);
}
