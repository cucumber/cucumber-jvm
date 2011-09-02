package cucumber.runtime.transformers;

import java.math.BigDecimal;
import java.util.Locale;

public class BigDecimalTransformer extends TransformerWithNumberFormat<BigDecimal> {

    public BigDecimalTransformer(Locale locale) {
        super(locale, new Class[]{BigDecimal.class});
    }

    @Override
    protected BigDecimal doTransform(Number argument) {
        // See http://java.sun.com/j2se/6/docs/api/java/math/BigDecimal.html#BigDecimal%28double%29
        return new BigDecimal(Double.toString(argument.doubleValue()));
    }

}
