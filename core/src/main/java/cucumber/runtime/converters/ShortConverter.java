package cucumber.runtime.converters;

import java.util.Locale;

public class ShortConverter extends ConverterWithNumberFormat<Short> {

    public ShortConverter(Locale locale) {
        super(locale, new Class[]{Short.class, Short.TYPE});
    }

    @Override
    protected Short downcast(Number argument) {
        return argument.shortValue();
    }

}
