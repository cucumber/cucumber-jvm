package cucumber.examples.java.calculator;

import cucumber.api.TypeRegistryConfigurer;
import cucumber.api.TypeRegistry;
import io.cucumber.datatable.DataTableType;
import cucumber.examples.java.calculator.RpnCalculatorStepdefs.Entry;
import cucumber.examples.java.calculator.ShoppingStepdefs.Grocery;
import io.cucumber.cucumberexpressions.ParameterType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static java.text.DateFormat.MEDIUM;
import static java.text.DateFormat.getDateInstance;
import static java.util.Locale.ENGLISH;

public class ParameterTypes implements TypeRegistryConfigurer {

    @Override
    public Locale locale() {
        return ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        typeRegistry.defineParameterType(new ParameterType<>(
                "date",
                "((.*) \\d{1,2}, \\d{4})",
                Date.class,
                (String s) -> getDateInstance(MEDIUM, ENGLISH).parse(s)
            )
        );

        typeRegistry.defineParameterType(new ParameterType<>(
            "iso-date",
            "\\d{4}-\\d{2}-\\d{2}",
            Date.class,
            (String s) -> new SimpleDateFormat("yyyy-mm-dd").parse(s)
        ));

        typeRegistry.defineDataTableType(new DataTableType(
            Entry.class,
            (Map<String, String> row) -> new Entry(
                Integer.valueOf(row.get("first")),
                Integer.valueOf(row.get("second")),
                row.get("operation")
            )
        ));

        typeRegistry.defineDataTableType(new DataTableType(
            Grocery.class,
            (Map<String, String> row) -> new Grocery(
                row.get("name"),
                ShoppingStepdefs.Price.fromString(row.get("price"))
            )
        ));
    }
}
