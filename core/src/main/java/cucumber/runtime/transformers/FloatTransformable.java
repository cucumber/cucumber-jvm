package cucumber.runtime.transformers;

public class FloatTransformable extends TransformableWithNumberFormat<Float> {

    @Override
    protected Float doTransform(Number argument) {
        return Float.valueOf(argument.floatValue());
    }

}
