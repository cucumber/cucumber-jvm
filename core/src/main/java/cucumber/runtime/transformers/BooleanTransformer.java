package cucumber.runtime.transformers;

import java.util.Locale;

public class BooleanTransformer implements Transformer<Boolean> {

    public Boolean transform(Locale locale, String string) throws TransformationException {
        if ("false".equalsIgnoreCase(string) || "true".equalsIgnoreCase(string)) {
            return Boolean.parseBoolean(string);
        } else {
            throw new TransformationException(String.format(locale, "Could not convert %s to Boolean", string));
        }
    }

}
