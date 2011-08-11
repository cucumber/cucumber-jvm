package cucumber.runtime.transformers;

public class IntegerTransformable extends TransformableWithNumberFormat<Integer> {

    @Override
    protected Integer doTransform(Number number) {
        return Integer.valueOf(number.intValue());
    }

}
