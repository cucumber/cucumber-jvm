package cucumber.runtime.transformers;

import java.util.Locale;

public class BooleanTransformer implements Transformer<Boolean> {

    public Boolean transform(Locale locale, String... arguments) throws TransformationException {
        if ("false".equalsIgnoreCase(arguments[0]) || "true".equalsIgnoreCase(arguments[0])) {
            return Boolean.parseBoolean(arguments[0]);
        } else {
            throw new TransformationException(String.format(locale, "Could not convert %s to Boolean", arguments[0]));
        }
    }

}
