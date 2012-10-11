package cucumber.runtime.xstream;

import java.util.Locale;

class IntegerConverter extends ConverterWithNumberFormat<Integer> {

    public IntegerConverter(Locale locale) {
        super(locale, new Class[]{Integer.class, Integer.TYPE});
    }

    @Override
    protected Integer downcast(Number argument) {
        return argument.intValue();
    }

}
