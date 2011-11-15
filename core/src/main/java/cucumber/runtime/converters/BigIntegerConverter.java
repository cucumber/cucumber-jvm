package cucumber.runtime.converters;

import java.math.BigInteger;
import java.util.Locale;

public class BigIntegerConverter extends ConverterWithNumberFormat<BigInteger> {

    public BigIntegerConverter(Locale locale) {
        super(locale, new Class[]{BigInteger.class});
    }

    @Override
    protected BigInteger downcast(Number argument) {
        return BigInteger.valueOf(argument.longValue());
    }

}
