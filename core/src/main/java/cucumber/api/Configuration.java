package cucumber.api;

import io.cucumber.java.TypeRegistry;

public interface Configuration {
    TypeRegistry createTypeRegistry();
}
