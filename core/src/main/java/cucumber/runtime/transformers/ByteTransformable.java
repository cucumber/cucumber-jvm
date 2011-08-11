package cucumber.runtime.transformers;

public class ByteTransformable extends TransformableWithNumberFormat<Byte> {

    @Override
    protected Byte doTransform(Number value) {
        return Byte.valueOf(value.byteValue());
    }

}
