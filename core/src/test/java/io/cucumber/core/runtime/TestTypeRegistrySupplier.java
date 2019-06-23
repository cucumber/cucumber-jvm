package io.cucumber.core.runtime;

import io.cucumber.core.stepexpression.TypeRegistry;

import java.util.Locale;

public class TestTypeRegistrySupplier implements  TypeRegistrySupplier {

    @Override
    public TypeRegistry get() {
        return new TypeRegistry(Locale.ENGLISH);
    }
}
