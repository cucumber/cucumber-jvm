package cucumber.runtime.transformers;

import java.util.Locale;

public class BooleanTransformable implements Transformable<Boolean> {

    public Boolean transform(String argument, Locale locale) {
        if ("false".equalsIgnoreCase(argument) || "true".equalsIgnoreCase(argument)) {
            return Boolean.parseBoolean(argument);
        } else {
            throw new TransformationException(String.format(locale, "Could not convert %s to Boolean", argument));
        }
    }

}
