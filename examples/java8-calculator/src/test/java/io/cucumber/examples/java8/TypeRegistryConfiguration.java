package io.cucumber.examples.java8;

import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.core.api.TypeRegistry;
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
            RpnCalculatorStepdefs.Entry.class,
            (Map<String, String> row) -> new RpnCalculatorStepdefs.Entry(
                Integer.valueOf(row.get("first")),
                Integer.valueOf(row.get("second")),
                row.get("operation")
            )
        ));

        typeRegistry.defineDataTableType(new DataTableType(
            ShoppingStepdefs.Grocery.class,
            (Map<String, String> row) -> new ShoppingStepdefs.Grocery(
                row.get("name"),
                ShoppingStepdefs.Price.fromString(row.get("price"))
            )
        ));
    }
}
