package cucumber.runtime.xstream;

import java.util.Locale;

class EnumConverter extends ConverterWithEnumFormat<Enum> {

    public EnumConverter(Locale locale, Class<? extends Enum> enumClass) {
        super(locale, enumClass);
    }
}
