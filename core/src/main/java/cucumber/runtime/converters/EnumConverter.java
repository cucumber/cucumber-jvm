package cucumber.runtime.converters;

import java.util.Locale;

public class EnumConverter extends ConverterWithEnumFormat<Enum> {

    public EnumConverter(Locale locale, Class<? extends Enum> enumClass) {
        super(locale, enumClass);
    }
}
