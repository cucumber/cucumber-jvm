package cucumber.api;

import io.cucumber.cucumberexpressions.TransformLookup;

public interface Configuration {
    TransformLookup createTransformLookup();
}
