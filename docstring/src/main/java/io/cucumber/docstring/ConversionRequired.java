package io.cucumber.docstring;

import io.cucumber.docstring.DocString.DocStringConverter;

import java.lang.reflect.Type;

final class ConversionRequired implements DocStringConverter {

    @Override
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <T> T convert(DocString docString, Type type) {
        throw new CucumberDocStringException(
            "Can't convert DocString to %s. You have to write the conversion for it in this method"
                    .formatted(type));
    }

}
