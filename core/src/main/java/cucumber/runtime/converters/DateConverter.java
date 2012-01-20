package cucumber.runtime.converters;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static java.util.Arrays.asList;

public class DateConverter extends ConverterWithFormat<Date> {
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private final List<DateFormat> formats = new ArrayList<DateFormat>();
    private SimpleDateFormat onlyFormat;

    public DateConverter(Locale locale) {
        super(new Class[]{Date.class});

        addFormat(DateFormat.SHORT, locale);
        addFormat(DateFormat.MEDIUM, locale);
        addFormat(DateFormat.LONG, locale);
        addFormat(DateFormat.FULL, locale);
    }

    public DateConverter(String dateFormatString, Locale locale) {
        super(new Class[]{Date.class});
        // TODO - these are expensive to create. Cache by format+string, or use the XStream DF cache util thingy
        add(new SimpleDateFormat(dateFormatString, locale));
    }

    private void addFormat(int style, Locale locale) {
        add(DateFormat.getDateInstance(style, locale));
    }

    private void add(DateFormat dateFormat) {
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(UTC);
        formats.add(dateFormat);
    }

    public List<? extends Format> getFormats() {
        return onlyFormat == null ? formats : asList(onlyFormat);
    }

    public void setOnlyFormat(String dateFormatString, Locale locale) {
        onlyFormat = new SimpleDateFormat(dateFormatString, locale);
        onlyFormat.setLenient(false);
        onlyFormat.setTimeZone(UTC);
    }

    public void removeOnlyFormat() {
        onlyFormat = null;
    }
}
