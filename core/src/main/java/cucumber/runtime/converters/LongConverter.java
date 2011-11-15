package cucumber.runtime.converters;

import java.util.Locale;

public class LongConverter extends ConverterWithNumberFormat<Long> {

    public LongConverter(Locale locale) {
        super(locale, new Class[]{Long.class, Long.TYPE});
    }

    @Override
    protected Long downcast(Number argument) {
        return argument.longValue();
    }

}
