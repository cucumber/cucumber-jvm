package cucumber.runtime.xstream;

import java.util.Date;
import java.util.Locale;

class DateConverter extends TimeConverter<Date> {
    public DateConverter(Locale locale) {
        super(locale, new Class[]{Date.class});
    }
}
