package cucumber.runtime.xstream;

import cucumber.runtime.ParameterInfo;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

abstract class TimeConverter<T> extends ConverterWithFormat<T> {
    private final List<DateFormat> formats = new ArrayList<DateFormat>();
    private String format;

    TimeConverter(Locale locale, Class[] convertibleTypes) {
        super(convertibleTypes);

        // TODO - these are expensive to create. Cache by format+string, or use the XStream DF cache util thingy
        addFormat(DateFormat.SHORT, locale);
        addFormat(DateFormat.MEDIUM, locale);
        addFormat(DateFormat.LONG, locale);
        addFormat(DateFormat.FULL, locale);
    }

    void addFormat(int style, Locale locale) {
        add(DateFormat.getDateInstance(style, locale));
    }

    void add(DateFormat dateFormat) {
        dateFormat.setLenient(false);
        formats.add(dateFormat);
    }

    public List<? extends Format> getFormats() {
        return format == null ? formats : asList(getOnlyFormat());
    }

    private Format getOnlyFormat() {
        DateFormat dateFormat = new SimpleDateFormat(format, getLocale());
        dateFormat.setLenient(false);

        return dateFormat;
    }

    @Override
    public String toString(Object obj) {
        if (obj instanceof Calendar) {
            obj = ((Calendar) obj).getTime();
        }
        return super.toString(obj);
    }

    @Override
    public void setParameterInfoAndLocale(ParameterInfo parameterInfo, Locale locale) {
        super.setParameterInfoAndLocale(parameterInfo, locale);

        if (parameterInfo.getFormat() != null) {
            format = parameterInfo.getFormat();
        }
    }

    public void removeOnlyFormat() {
        format = null;
    }

    public static List<Class> getTimeClasses() {
        List<Class> classes = new ArrayList<Class>();
        classes.add(Date.class);
        classes.add(Calendar.class);
        return classes;
    }
}
