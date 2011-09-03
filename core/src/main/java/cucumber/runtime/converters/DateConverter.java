package cucumber.runtime.converters;

import java.text.DateFormat;
import java.text.Format;
import java.util.*;

public class DateConverter extends ConverterWithFormat<Date> {
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private final List<Format> formats = new ArrayList<Format>();

    public DateConverter(Locale locale) {
        super(new Class[]{Date.class});

        addFormat(locale, DateFormat.SHORT);
        addFormat(locale, DateFormat.MEDIUM);
        addFormat(locale, DateFormat.LONG);
        addFormat(locale, DateFormat.FULL);
    }

    private void addFormat(Locale locale, int aShort) {
        DateFormat shortFormat = DateFormat.getDateInstance(aShort, locale);
        shortFormat.setLenient(false);
        shortFormat.setTimeZone(UTC);
        formats.add(shortFormat);
    }

    public List<Format> getFormats() {
        return formats;
    }

}
