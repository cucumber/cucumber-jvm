package cucumber.examples.java.calculator;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.Configuration;
import io.cucumber.cucumberexpressions.Function;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.SingleTransformer;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableRowTransformer;
import io.cucumber.java.TypeRegistry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static java.text.DateFormat.MEDIUM;
import static java.text.DateFormat.getDateInstance;
import static java.util.Locale.ENGLISH;

public class ParameterTypes implements Configuration {

    @Override
    public TypeRegistry createTypeRegistry() {
        TypeRegistry parameterTypeRegistry = new TypeRegistry(ENGLISH);
        parameterTypeRegistry.defineParameterType(new ParameterType<Date>(
            "date",
            "((.*) \\d{1,2}, \\d{4})",
            Date.class,
            new SingleTransformer<Date>(new Function<String, Date>() {
                @Override
                public Date apply(String text) throws ParseException {
                    return getDateInstance(MEDIUM, ENGLISH).parse(text);
                }
            })
        ));

        parameterTypeRegistry.defineParameterType(new ParameterType<Date>(
            "iso-date",
            "\\d{4}-\\d{2}-\\d{2}",
            Date.class,
            new SingleTransformer<Date>(new Function<String, Date>() {
                @Override
                public Date apply(String text) throws ParseException {
                    return new SimpleDateFormat("yyyy-mm-dd").parse(text);
                }
            })
        ));

        parameterTypeRegistry.defineDataTableType(DataTableType.tableOf("entry", RpnCalculatorStepdefs.Entry.class, new TableRowTransformer<RpnCalculatorStepdefs.Entry>() {
            @Override
            public RpnCalculatorStepdefs.Entry transform(Map<String, String> row) {
                return new ObjectMapper().convertValue(row, RpnCalculatorStepdefs.Entry.class);
            }
        }));

        parameterTypeRegistry.defineDataTableType(DataTableType.tableOf("groceries", ShoppingStepdefs.Grocery.class, new TableRowTransformer<ShoppingStepdefs.Grocery>() {
            @Override
            public ShoppingStepdefs.Grocery transform(Map<String, String> row) {
                return new ObjectMapper().convertValue(row, ShoppingStepdefs.Grocery.class);
            }
        }));

        return parameterTypeRegistry;
    }
}
