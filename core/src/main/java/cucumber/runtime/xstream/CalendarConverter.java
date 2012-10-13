package cucumber.runtime.xstream;

import java.text.Format;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

class CalendarConverter extends TimeConverter<Calendar> {
    public CalendarConverter(Locale locale) {
        super(locale, new Class[]{Calendar.class});
    }

    @Override
    protected Object transform(Format format, String argument) {
        Date date = (Date) super.transform(format, argument);
        Calendar cal = Calendar.getInstance(getLocale());
        cal.setTime(date);
        return cal;
    }
}
