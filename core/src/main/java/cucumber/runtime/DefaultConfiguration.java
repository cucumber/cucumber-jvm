package cucumber.runtime;

import cucumber.api.Configuration;
import io.cucumber.cucumberexpressions.TransformLookup;

import java.util.Locale;

public class DefaultConfiguration implements Configuration {
    @Override
    public TransformLookup createTransformLookup() {
        return new TransformLookup(Locale.ENGLISH);
    }
}
