package cucumber.runtime.converters;

import cucumber.runtime.CucumberException;
import cucumber.runtime.ParameterType;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

public abstract class TimeConverter<T> extends ConverterWithFormat<T> {
    protected final Locale locale;
    private final List<DateFormat> formats = new ArrayList<DateFormat>();
    private SimpleDateFormat onlyFormat;

    public TimeConverter(Locale locale, Class[] convertibleTypes) {
        super(convertibleTypes);
        this.locale = locale;

        // TODO - these are expensive to create. Cache by format+string, or use the XStream DF cache util thingy
        addFormat(DateFormat.SHORT, locale);
        addFormat(DateFormat.MEDIUM, locale);
        addFormat(DateFormat.LONG, locale);
        addFormat(DateFormat.FULL, locale);
    }

    protected void addFormat(int style, Locale locale) {
        add(DateFormat.getDateInstance(style, locale));
    }

    protected void add(DateFormat dateFormat) {
        dateFormat.setLenient(false);
        formats.add(dateFormat);
    }

    public List<? extends Format> getFormats() {
        return onlyFormat == null ? formats : asList(onlyFormat);
    }

    public void setOnlyFormat(String dateFormatString, Locale locale) {
        onlyFormat = new SimpleDateFormat(dateFormatString, locale);
        onlyFormat.setLenient(false);
    }

    public void removeOnlyFormat() {
        onlyFormat = null;
    }

    public static TimeConverter getInstance(ParameterType parameterType, Locale locale) {
        if (Date.class.isAssignableFrom(parameterType.getRawType())) {
            return new DateConverter(locale);
        } else if (Calendar.class.isAssignableFrom(parameterType.getRawType())) {
            return new CalendarConverter(locale);
        } else {
            throw new CucumberException("Unsupported time type: " + parameterType.getRawType());
        }
    }

    public static List<Class> getTimeClasses() {
        List<Class> classes = new ArrayList<Class>();
        classes.add(Date.class);
        classes.add(Calendar.class);
        return classes;
    }
}
