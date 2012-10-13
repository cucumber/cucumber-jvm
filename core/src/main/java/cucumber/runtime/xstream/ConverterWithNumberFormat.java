package cucumber.runtime.xstream;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

abstract class ConverterWithNumberFormat<T extends Number> extends ConverterWithFormat<T> {
    private final List<NumberFormat> formats = new ArrayList<NumberFormat>();

    ConverterWithNumberFormat(Locale locale, Class[] convertibleTypes) {
        super(convertibleTypes);
        formats.add(NumberFormat.getNumberInstance(locale));
    }

    @Override
    public T transform(String string) {
        T number = super.transform(string);
        return number == null ? null : downcast(number);
    }

    @Override
    public List<NumberFormat> getFormats() {
        return formats;
    }

    protected abstract T downcast(Number argument);
}
