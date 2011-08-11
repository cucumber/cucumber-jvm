package cucumber.runtime.transformers;

import java.text.DateFormat;
import java.text.Format;
import java.util.Date;
import java.util.Locale;

public class DateTransformable extends TransformableWithFormat<Date> {

    public Format getFormat(Locale locale) {
        DateFormat format = DateFormat
                .getDateInstance(DateFormat.SHORT, locale);
        format.setLenient(false);
        return format;
    }

}
