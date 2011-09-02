package cucumber.runtime.transformers;

import java.text.DateFormat;
import java.text.Format;
import java.util.Date;
import java.util.Locale;

public class DateTransformer extends TransformerWithFormat<Date> {

    public DateTransformer(Locale locale) {
        super(locale, new Class[]{Date.class});
    }

    public Format getFormat(Locale locale) {
        // TODO: pass in format somehow
        DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        format.setLenient(false);
        return format;
    }

}
