package cucumber.runtime.transformers;

public class ShortTransformable extends TransformableWithNumberFormat<Short> {

	@Override
	protected Short doTransform(Number argument) {
		return Short.valueOf(argument.shortValue());
	}

}
