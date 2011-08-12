package cucumber.runtime.transformers;

public class ByteTransformer extends TransformerWithNumberFormat<Byte> {

    @Override
    protected Byte doTransform(Number argument) {
        return argument.byteValue();
    }

}
