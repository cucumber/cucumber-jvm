package cucumber.cukeulator.test;

import java.util.Locale;

import cucumber.api.TypeRegistry;
import cucumber.api.TypeRegistryConfigurer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import io.cucumber.cucumberexpressions.Transformer;

import static java.text.DateFormat.getDateInstance;
import static java.util.Locale.ENGLISH;

public class TypeRegistryConfiguration implements TypeRegistryConfigurer {

    @Override
    public Locale locale() {
        return ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        typeRegistry.defineParameterType(new ParameterType<Integer>(
                "digit",
                "[0-9]",
                Integer.class,
                new Transformer<Integer>() {
                    @Override
                    public Integer transform(String text) {
                        return Integer.parseInt(text);
                    }
                })
        );

        typeRegistry.defineParameterType(new ParameterType<Character>(
                "operator",
                "[+–x\\/=]",
                Character.class,
                new Transformer<Character>() {
                    @Override
                    public Character transform(String text) {
                        return text.charAt(0);
                    }
                })
        );
    }
}
