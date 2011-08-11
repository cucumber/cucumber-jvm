package cucumber.runtime.transformers;

import java.math.BigInteger;

public class BigIntegerTransformable extends TransformableWithNumberFormat<BigInteger> {

    @Override
    protected BigInteger doTransform(Number argument) {
        return BigInteger.valueOf(argument.longValue());
    }

}
