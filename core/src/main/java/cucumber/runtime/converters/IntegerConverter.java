package cucumber.runtime.converters;

import java.util.Locale;

public class IntegerConverter extends ConverterWithNumberFormat<Integer> {

    public IntegerConverter(Locale locale) {
        super(locale, new Class[]{Integer.class, Integer.TYPE});
    }

    @Override
    protected Integer doTransform(Number argument) {
        return argument.intValue();
    }

}
