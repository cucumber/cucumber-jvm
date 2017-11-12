package cucumber.examples.java.calculator;

import cucumber.api.Configuration;
import io.cucumber.cucumberexpressions.Function;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.SingleTransformer;
import io.cucumber.java.TypeRegistry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
                public Date apply(String text) {
                    try {
                        return getDateInstance(MEDIUM, ENGLISH).parse(text);
                    } catch (ParseException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            })
        ));

        parameterTypeRegistry.defineParameterType(new ParameterType<Date>(
            "iso-date",
            "\\d{4}-\\d{2}-\\d{2}",
            Date.class,
            new SingleTransformer<Date>(new Function<String, Date>() {
                @Override
                public Date apply(String text) {
                    try {
                        return new SimpleDateFormat("yyyy-mm-dd").parse(text);
                    } catch (ParseException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            })
        ));

        return parameterTypeRegistry;
    }
}
