package cucumber.runtime.transformers;

public class LongTransformer extends TransformerWithNumberFormat<Long> {

    @Override
    protected Long doTransform(Number argument) {
        return argument.longValue();
    }

}
