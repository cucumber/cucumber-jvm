package cucumber.runtime.converters;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class ConverterWithNumberFormat<T extends Number> extends ConverterWithFormat<T> {
    private final List<NumberFormat> formats = new ArrayList<NumberFormat>();

    public ConverterWithNumberFormat(Locale locale, Class[] convertibleTypes) {
        super(convertibleTypes);
        formats.add(NumberFormat.getNumberInstance(locale));
    }

    @Override
    public T fromString(String string) {
        return doTransform(super.fromString(string));
    }

    @Override
    public List<NumberFormat> getFormats() {
        return formats;
    }

    protected abstract T doTransform(Number argument);
}
