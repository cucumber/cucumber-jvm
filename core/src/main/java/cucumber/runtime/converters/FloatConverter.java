package cucumber.runtime.converters;

import java.util.Locale;

public class FloatConverter extends ConverterWithNumberFormat<Float> {

    public FloatConverter(Locale locale) {
        super(locale, new Class[]{Float.class, Float.TYPE});
    }

    @Override
    protected Float downcast(Number argument) {
        return argument.floatValue();
    }

}
