package cucumber.runtime.converters;

import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;

public abstract class ConverterWithNumberFormat<T extends Number> extends ConverterWithFormat<T> {

    public ConverterWithNumberFormat(Locale locale, Class[] convertibleTypes) {
        super(locale, convertibleTypes);
    }

    @Override
    public T fromString(String string) {
        return doTransform(super.fromString(string));
    }

    @Override
    public Format getFormat(Locale locale) {
        return NumberFormat.getNumberInstance(locale);
    }

    protected abstract T doTransform(Number argument);
}
