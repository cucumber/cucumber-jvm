package cucumber.runtime.transformers;

public class FloatTransformer extends TransformerWithNumberFormat<Float> {

    @Override
    protected Float doTransform(Number argument) {
        return argument.floatValue();
    }

}
