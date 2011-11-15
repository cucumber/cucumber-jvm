package cucumber.runtime.converters;

import java.util.Locale;

public class ByteConverter extends ConverterWithNumberFormat<Byte> {

    public ByteConverter(Locale locale) {
        super(locale, new Class[]{Byte.class, Byte.TYPE});
    }

    @Override
    protected Byte downcast(Number argument) {
        return argument.byteValue();
    }

}
