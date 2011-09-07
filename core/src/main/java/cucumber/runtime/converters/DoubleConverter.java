package cucumber.runtime.converters;

import java.util.Locale;

public class DoubleConverter extends ConverterWithNumberFormat<Double> {

    public DoubleConverter(Locale locale) {
        super(locale, new Class[]{Double.class, Double.TYPE});
    }

    @Override
    protected Double doTransform(Number argument) {
        return argument.doubleValue();
    }

}
