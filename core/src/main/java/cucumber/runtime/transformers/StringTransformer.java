package cucumber.runtime.transformers;

import java.util.Locale;

public class StringTransformer implements Transformer<String> {

    public String transform(Locale locale, String string) {
        return string;
    }

}
