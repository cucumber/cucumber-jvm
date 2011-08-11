package cucumber.runtime.transformers;

public class DoubleTransformer extends TransformerWithNumberFormat<Double> {

    @Override
    protected Double doTransform(Number argument) {
        return argument.doubleValue();
    }

}
