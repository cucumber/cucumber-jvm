package cucumber.runtime.xstream;

import java.util.Locale;

class FloatConverter extends ConverterWithNumberFormat<Float> {

    public FloatConverter(Locale locale) {
        super(locale, new Class[]{Float.class, Float.TYPE});
    }

    @Override
    protected Float downcast(Number argument) {
        return argument.floatValue();
    }

}
