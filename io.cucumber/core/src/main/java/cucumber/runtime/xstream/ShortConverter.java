package cucumber.runtime.xstream;

import java.util.Locale;

class ShortConverter extends ConverterWithNumberFormat<Short> {

    public ShortConverter(Locale locale) {
        super(locale, new Class[]{Short.class, Short.TYPE});
    }

    @Override
    protected Short downcast(Number argument) {
        return argument.shortValue();
    }

}
