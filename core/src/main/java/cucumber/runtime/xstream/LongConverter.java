package cucumber.runtime.xstream;

import java.util.Locale;

class LongConverter extends ConverterWithNumberFormat<Long> {

    public LongConverter(Locale locale) {
        super(locale, new Class[]{Long.class, Long.TYPE});
    }

    @Override
    protected Long downcast(Number argument) {
        return argument.longValue();
    }

}
