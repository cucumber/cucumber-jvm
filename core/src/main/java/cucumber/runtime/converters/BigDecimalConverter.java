package cucumber.runtime.converters;

import java.math.BigDecimal;
import java.util.Locale;

public class BigDecimalConverter extends ConverterWithNumberFormat<BigDecimal> {

    public BigDecimalConverter(Locale locale) {
        super(locale, new Class[]{BigDecimal.class});
    }

    @Override
    protected BigDecimal doTransform(Number argument) {
        // See http://java.sun.com/j2se/6/docs/api/java/math/BigDecimal.html#BigDecimal%28double%29
        return new BigDecimal(Double.toString(argument.doubleValue()));
    }

}
