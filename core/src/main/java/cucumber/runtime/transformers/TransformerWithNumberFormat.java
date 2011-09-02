package cucumber.runtime.transformers;

import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;

public abstract class TransformerWithNumberFormat<T extends Number> extends TransformerWithFormat<T> {

    public TransformerWithNumberFormat(Locale locale, Class[] convertibleTypes) {
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
