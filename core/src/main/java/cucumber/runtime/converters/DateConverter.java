package cucumber.runtime.converters;

import java.util.Date;
import java.util.Locale;

public class DateConverter extends TimeConverter<Date> {
    public DateConverter(Locale locale) {
        super(locale, new Class[]{Date.class});
    }
}
