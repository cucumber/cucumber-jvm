package cucumber.runtime.transformers;

import java.math.BigInteger;

public class BigIntegerTransformer extends TransformerWithNumberFormat<BigInteger> {

    @Override
    protected BigInteger doTransform(Number argument) {
        return BigInteger.valueOf(argument.longValue());
    }

}
