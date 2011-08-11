package cucumber.runtime.transformers;

import java.util.Locale;

public class StringTransformer implements Transformer<String> {

    public String transform(String argument, Locale locale) {
        return argument;
    }

}
