package cucumber.examples.java.calculator;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.Configuration;
import cucumber.examples.java.calculator.ShoppingStepdefs.Grocery;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTableType;
import io.cucumber.java.TypeRegistry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static java.text.DateFormat.MEDIUM;
import static java.text.DateFormat.getDateInstance;
import static java.util.Locale.ENGLISH;

public class ParameterTypes implements Configuration {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public TypeRegistry createTypeRegistry() {
        TypeRegistry parameterTypeRegistry = new TypeRegistry(ENGLISH);
        parameterTypeRegistry.defineParameterType(new ParameterType<>(
                "date",
                "((.*) \\d{1,2}, \\d{4})",
                Date.class,
                (String s) -> getDateInstance(MEDIUM, ENGLISH).parse(s)
            )
        );

        parameterTypeRegistry.defineParameterType(new ParameterType<>(
            "iso-date",
            "\\d{4}-\\d{2}-\\d{2}",
            Date.class,
            (String s) -> new SimpleDateFormat("yyyy-mm-dd").parse(s)
        ));

        parameterTypeRegistry.defineDataTableType(new DataTableType(
            "entry",
            RpnCalculatorStepdefs.Entry.class,
            (Map<String, String> row) -> objectMapper.convertValue(row, RpnCalculatorStepdefs.Entry.class)));

        parameterTypeRegistry.defineDataTableType(new DataTableType(
            "groceries",
            Grocery.class,
            (Map<String, String> row) -> objectMapper.convertValue(row, Grocery.class)));

        return parameterTypeRegistry;
    }
}
