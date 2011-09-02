package cucumber.runtime.transformers;

import java.util.Locale;

public class DoubleTransformer extends TransformerWithNumberFormat<Double> {

    public DoubleTransformer(Locale locale) {
        super(locale, new Class[]{Double.class, Double.TYPE});
    }

    @Override
    protected Double doTransform(Number argument) {
        return argument.doubleValue();
    }

}
