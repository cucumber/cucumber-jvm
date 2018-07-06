package cucumber.example.android.cukeulator.test;

import cucumber.api.TypeRegistryConfigurer;
import cucumber.api.TypeRegistry;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.Transformer;

import java.util.Locale;

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
                public Integer transform(String s) throws Throwable {
                    return Integer.parseInt(s);
                }
            })
        );

        typeRegistry.defineParameterType(new ParameterType<Character>(
            "operator",
            "[+–x\\/=]",
            Character.class,
            new Transformer<Character>() {
                @Override
                public Character transform(String s) throws Throwable {
                    return s.charAt(0);
                }
            })
        );
    }
}
