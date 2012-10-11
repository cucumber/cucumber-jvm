package cucumber.runtime.xstream;

import java.util.Locale;

class ByteConverter extends ConverterWithNumberFormat<Byte> {

    public ByteConverter(Locale locale) {
        super(locale, new Class[]{Byte.class, Byte.TYPE});
    }

    @Override
    protected Byte downcast(Number argument) {
        return argument.byteValue();
    }

}
