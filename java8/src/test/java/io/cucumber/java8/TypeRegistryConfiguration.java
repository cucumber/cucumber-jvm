package io.cucumber.java8;

import io.cucumber.core.api.TypeRegistry;
import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.TypeReference;
import io.cucumber.datatable.DataTableType;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Locale.ENGLISH;

public class TypeRegistryConfiguration implements TypeRegistryConfigurer {

    @Override
    public Locale locale() {
        return ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        typeRegistry.defineDataTableType(new DataTableType(
            LambdaStepDefinitions.Person.class,
            (Map<String, String> map) -> {
                LambdaStepDefinitions.Person person = new LambdaStepDefinitions.Person();
                person.first = map.get("first");
                person.last = map.get("last");
                return person;
            }));

        typeRegistry.defineParameterType(new ParameterType<>(
            "optional",
            Collections.singletonList("[a-z]*"),
            new TypeReference<Optional<String>>() {
            }.getType(),
            (String args) -> Optional.of(args), false, false
        ));
    }
}
