package cucumber.runtime.transformers;

public class ShortTransformer extends TransformerWithNumberFormat<Short> {

    @Override
    protected Short doTransform(Number argument) {
        return argument.shortValue();
    }

}
