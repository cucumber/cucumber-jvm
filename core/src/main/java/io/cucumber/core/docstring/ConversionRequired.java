package io.cucumber.core.docstring;

import io.cucumber.core.docstring.DocString.DocStringConverter;
import io.cucumber.core.exception.CucumberException;

import java.lang.reflect.Type;

import static java.lang.String.format;

final class ConversionRequired implements DocStringConverter {

    @Override
    public <T> T convert(DocString docString, Type type) {
        throw new CucumberException(format("" +
                "Can't convert DocString to %s. " +
                "You have to write the conversion for it in this method",
            type
        ));
    }
}
