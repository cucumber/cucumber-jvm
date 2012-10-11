package cucumber.runtime.xstream;

import java.util.Locale;

class DoubleConverter extends ConverterWithNumberFormat<Double> {

    public DoubleConverter(Locale locale) {
        super(locale, new Class[]{Double.class, Double.TYPE});
    }

    @Override
    protected Double downcast(Number argument) {
        return argument.doubleValue();
    }

}
