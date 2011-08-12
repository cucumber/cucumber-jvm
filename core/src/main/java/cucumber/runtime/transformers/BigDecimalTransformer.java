package cucumber.runtime.transformers;

import java.math.BigDecimal;

public class BigDecimalTransformer extends TransformerWithNumberFormat<BigDecimal> {

    @Override
    protected BigDecimal doTransform(Number argument) {
        // See http://java.sun.com/j2se/6/docs/api/java/math/BigDecimal.html#BigDecimal%28double%29
        return new BigDecimal(Double.toString(argument.doubleValue()));
    }

}
