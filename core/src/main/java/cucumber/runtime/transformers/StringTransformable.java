package cucumber.runtime.transformers;

import java.util.Locale;

public class StringTransformable implements Transformable<String> {

    public String transform(String argument, Locale locale) {
        return argument;
    }

}
