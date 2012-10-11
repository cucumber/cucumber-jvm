package cucumber.runtime.xstream;

import java.math.BigInteger;
import java.util.Locale;

class BigIntegerConverter extends ConverterWithNumberFormat<BigInteger> {

    public BigIntegerConverter(Locale locale) {
        super(locale, new Class[]{BigInteger.class});
    }

    @Override
    protected BigInteger downcast(Number argument) {
        return BigInteger.valueOf(argument.longValue());
    }

}
