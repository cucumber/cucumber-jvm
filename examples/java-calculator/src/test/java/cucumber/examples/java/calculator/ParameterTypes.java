package cucumber.examples.java.calculator;

import cucumber.api.Configuration;
import io.cucumber.cucumberexpressions.Function;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import io.cucumber.cucumberexpressions.SingleTransformer;

import java.text.ParseException;
import java.util.Date;

import static java.text.DateFormat.MEDIUM;
import static java.text.DateFormat.getDateInstance;
import static java.util.Locale.ENGLISH;

public class ParameterTypes implements Configuration {

    @Override
    public ParameterTypeRegistry createParameterTypeRegistry() {
        ParameterTypeRegistry parameterTypeRegistry = new ParameterTypeRegistry(ENGLISH);
        parameterTypeRegistry.defineParameterType(new ParameterType<Date>(
            "date",
            "((.*) \\d{1,2}, \\d{4})",
            Date.class,
            new SingleTransformer<Date>(new Function<String, Date>() {
                @Override
                public Date apply(String text) {
                    try {
                        return getDateInstance(MEDIUM, ENGLISH).parse(text);
                    } catch (ParseException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            })
        ));

        return parameterTypeRegistry;
    }
}
