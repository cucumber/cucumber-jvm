package cucumber.examples.java.calculator;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.Configuration;
import cucumber.api.TypeRegistry;
import cucumber.api.datatable.DataTableType;
import cucumber.examples.java.calculator.RpnCalculatorStepdefs.Entry;
import cucumber.examples.java.calculator.ShoppingStepdefs.Grocery;

import java.util.Map;

import static java.util.Locale.ENGLISH;

public class ParameterTypes implements Configuration {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public TypeRegistry createTypeRegistry() {
        TypeRegistry parameterTypeRegistry = new TypeRegistry(ENGLISH);

        parameterTypeRegistry.defineDataTableType(new DataTableType(
            "entry",
            Entry.class,
            (Map<String, String> row) -> objectMapper.convertValue(row, Entry.class)));

        parameterTypeRegistry.defineDataTableType(new DataTableType(
            "groceries",
            Grocery.class,
            (Map<String, String> row) -> objectMapper.convertValue(row, Grocery.class)));

        return parameterTypeRegistry;
    }
}
