package io.cucumber.docstring;

import io.cucumber.docstring.DocString.DocStringConverter;

import java.lang.reflect.Type;

import static java.lang.String.format;

final class ConversionRequired implements DocStringConverter {

    @Override
    public <T> T convert(DocString docString, Type type) {
        throw new CucumberDocStringException(format("" +
                "Can't convert DocString to %s. " +
                "You have to write the conversion for it in this method",
            type));
    }

}
