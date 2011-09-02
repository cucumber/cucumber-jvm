package cucumber.runtime.converters;

import java.text.DateFormat;
import java.text.Format;
import java.util.Date;
import java.util.Locale;

public class DateConverter extends ConverterWithFormat<Date> {

    public DateConverter(Locale locale) {
        super(locale, new Class[]{Date.class});
    }

    public Format getFormat(Locale locale) {
        // TODO: pass in format somehow
        DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        format.setLenient(false);
        return format;
    }

}
