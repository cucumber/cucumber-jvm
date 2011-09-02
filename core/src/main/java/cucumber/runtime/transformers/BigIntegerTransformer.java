package cucumber.runtime.transformers;

import java.math.BigInteger;
import java.util.Locale;

public class BigIntegerTransformer extends TransformerWithNumberFormat<BigInteger> {

    public BigIntegerTransformer(Locale locale) {
        super(locale, new Class[]{BigInteger.class});
    }

    @Override
    protected BigInteger doTransform(Number argument) {
        return BigInteger.valueOf(argument.longValue());
    }

}
