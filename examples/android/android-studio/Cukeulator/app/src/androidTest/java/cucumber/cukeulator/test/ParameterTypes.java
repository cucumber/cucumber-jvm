package cucumber.cukeulator.test;

import cucumber.api.Configuration;
import io.cucumber.cucumberexpressions.Function;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import io.cucumber.cucumberexpressions.SingleTransformer;

import static java.text.DateFormat.getDateInstance;
import static java.util.Locale.ENGLISH;

public class ParameterTypes implements Configuration {

    @Override
    public ParameterTypeRegistry createParameterTypeRegistry() {
        ParameterTypeRegistry typeRegistry = new ParameterTypeRegistry(ENGLISH);
        typeRegistry.defineParameterType(new ParameterType<Integer>(
            "digit",
            "[0-9]",
            Integer.class,
            new SingleTransformer<Integer>(new Function<String, Integer>() {
                @Override
                public Integer apply(String text) {
                    return Integer.parseInt(text);
                }
            })
        ));

        typeRegistry.defineParameterType(new ParameterType<Character>(
            "operator",
            "[+–x\\/=]",
            Character.class,
            new SingleTransformer<Character>(new Function<String, Character>() {
                @Override
                public Character apply(String text) {
                    return text.charAt(0);
                }
            })
        ));


        return typeRegistry;
    }
}
