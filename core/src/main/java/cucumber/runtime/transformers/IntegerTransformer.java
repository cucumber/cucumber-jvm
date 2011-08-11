package cucumber.runtime.transformers;

public class IntegerTransformer extends TransformerWithNumberFormat<Integer> {

    @Override
    protected Integer doTransform(Number argument) {
        return argument.intValue();
    }

}
