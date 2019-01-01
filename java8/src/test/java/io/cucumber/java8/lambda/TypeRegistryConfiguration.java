package io.cucumber.java8.lambda;

import io.cucumber.core.api.TypeRegistry;
import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.datatable.DataTableType;

import java.util.Locale;
import java.util.Map;

import static java.util.Locale.ENGLISH;

public class TypeRegistryConfiguration implements TypeRegistryConfigurer {

    @Override
    public Locale locale() {
        return ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        typeRegistry.defineDataTableType(new DataTableType(
            LambdaStepdefs.Person.class,
            (Map<String, String> map) -> {
                LambdaStepdefs.Person person = new LambdaStepdefs.Person();
                person.first = map.get("first");
                person.last = map.get("last");
                return person;
            }));
    }
}
